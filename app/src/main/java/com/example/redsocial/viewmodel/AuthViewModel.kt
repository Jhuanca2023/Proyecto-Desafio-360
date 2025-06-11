package com.example.redsocial.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.redsocial.RedSocialApp
import kotlinx.coroutines.delay

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
    object Guest : AuthState()
    data class NeedsIntereses(val user: FirebaseUser?) : AuthState()
    object RegistrationCompleted : AuthState()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userData = MutableStateFlow<Map<String, Any>?>(null)
    val userData: StateFlow<Map<String, Any>?> = _userData.asStateFlow()

    private val _hasIntereses = MutableStateFlow<Boolean?>(null)
    val hasIntereses: StateFlow<Boolean?> = _hasIntereses.asStateFlow()

    init {
        checkAuthState()
        observeUserData()
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        _currentUser.value = currentUser
        
        if (currentUser != null) {
            viewModelScope.launch {
                try {
                    val userDoc = db.collection(RedSocialApp.COLLECTION_USUARIOS)
                        .document(currentUser.uid)
                        .get()
                        .await()

                    if (userDoc.exists()) {
                        val intereses = userDoc.get("intereses") as? List<*>
                        if (!intereses.isNullOrEmpty()) {
                            _authState.value = AuthState.Success(currentUser)
                        } else {
                            _authState.value = AuthState.NeedsIntereses(currentUser)
                        }
                    } else {
                        _authState.value = AuthState.NeedsIntereses(currentUser)
                    }
                } catch (e: Exception) {
                    _mensaje.value = "Error al verificar estado: ${e.message}"
                    _authState.value = AuthState.Error("Error al verificar estado de autenticación")
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    private fun observeUserData() {
        viewModelScope.launch {
            _currentUser.collect { user ->
                if (user != null) {
                    try {
                        val snapshot = db.collection(RedSocialApp.COLLECTION_USUARIOS)
                            .document(user.uid)
                            .get()
                            .await()
                        
                        if (snapshot.exists()) {
                            _userData.value = snapshot.data
                            val intereses = snapshot.get("intereses") as? List<*>
                            _hasIntereses.value = !intereses.isNullOrEmpty()
                        }
                    } catch (e: Exception) {
                        val errorMessage = when {
                            e.message?.contains("offline") == true -> "Error de conexión: Verifica tu conexión a internet"
                            e.message?.contains("network") == true -> "Error de red: Verifica tu conexión"
                            else -> "Error al obtener datos del usuario: ${e.message}"
                        }
                        _mensaje.value = errorMessage
                    }
                } else {
                    _userData.value = null
                    _hasIntereses.value = null
                }
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                checkAuthState()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar sesión")
                _mensaje.value = e.message ?: "Error al iniciar sesión"
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.sendPasswordResetEmail(email).await()
                _mensaje.value = "Se ha enviado un correo para restablecer tu contraseña"
                _authState.value = AuthState.Initial
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al enviar el correo")
                _mensaje.value = e.message ?: "Error al enviar el correo de recuperación"
            }
        }
    }

    fun validatePassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
        return passwordPattern.matches(password)
    }

    fun validateUsername(username: String): Boolean {
        return username.length >= 3 && !username.contains(" ")
    }

    fun checkUsernameAvailability(username: String, onResult: (Boolean) -> Unit) {
        db.collection("usuarios")
            .whereEqualTo("nombreUsuario", username)
            .get()
            .addOnSuccessListener { documents ->
                onResult(documents.isEmpty)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun registerWithEmail(
        email: String,
        password: String,
        nombreCompleto: String,
        nombreUsuario: String,
        fechaNacimiento: String,
        genero: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (!validatePassword(password)) {
            _mensaje.value = "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y caracteres especiales"
            _authState.value = AuthState.Error("Contraseña inválida")
            onError?.invoke("Contraseña inválida")
            return
        }

        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                _mensaje.value = "Creando cuenta..."
                
                // Primero creamos la cuenta de autenticación
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                if (user != null) {
                    try {
                        // Ahora que el usuario está autenticado, verificamos el nombre de usuario
                        _mensaje.value = "Verificando disponibilidad del nombre de usuario..."
                        val uniqueUsername = generateUniqueUsername(nombreUsuario)
                        
                        // Creamos el perfil del usuario
                        val userData = mapOf(
                            "email" to email,
                            "nombreCompleto" to nombreCompleto,
                            "nombreUsuario" to uniqueUsername,
                            "fechaNacimiento" to fechaNacimiento,
                            "genero" to genero,
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "intereses" to listOf<String>(),
                            "fechaRegistro" to com.google.firebase.Timestamp.now()
                        )

                        db.collection(RedSocialApp.COLLECTION_USUARIOS)
                            .document(user.uid)
                            .set(userData)
                            .await()

                        // Cerramos sesión para que el usuario tenga que hacer login explícitamente
                        auth.signOut()
                        _mensaje.value = "¡Registro exitoso! Por favor, inicia sesión con tu correo y contraseña."
                        _authState.value = AuthState.RegistrationCompleted
                        onSuccess?.invoke()
                    } catch (e: Exception) {
                        // Si falla la creación del perfil, eliminamos la cuenta de autenticación
                        try {
                            user.delete().await()
                        } catch (_: Exception) {}
                        
                        throw e
                    }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("email-already-in-use") == true ->
                        "Este correo electrónico ya está registrado"
                    e.message?.contains("invalid-email") == true ->
                        "El formato del correo electrónico no es válido"
                    e.message?.contains("weak-password") == true ->
                        "La contraseña es demasiado débil"
                    e.message?.contains("permission-denied") == true ->
                        "Error de permisos al crear el perfil"
                    e.message?.contains("offline") == true ->
                        "Error de conexión: Verifica tu conexión a internet"
                    else -> "Error en el registro: ${e.message}"
                }
                _mensaje.value = errorMsg
                _authState.value = AuthState.Error(errorMsg)
                onError?.invoke(errorMsg)
            }
        }
    }

    private suspend fun generateUniqueUsername(baseUsername: String): String {
        var username = baseUsername.replace("@", "").replace(".", "")
        var counter = 1
        var isUnique = false
        var finalUsername = username

        while (!isUnique) {
            val query = db.collection(RedSocialApp.COLLECTION_USUARIOS)
                .whereEqualTo("nombreUsuario", finalUsername)
                .get()
                .await()

            if (query.isEmpty) {
                isUnique = true
            } else {
                finalUsername = "${username}${counter}"
                counter++
            }
        }
        return finalUsername
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                _mensaje.value = "Verificando credenciales..."
                
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user

                if (user != null) {
                    _mensaje.value = "Autenticación exitosa, verificando perfil..."
                    try {
                        val userDoc = db.collection(RedSocialApp.COLLECTION_USUARIOS)
                            .document(user.uid)
                            .get()
                            .await()

                        if (!userDoc.exists()) {
                            _mensaje.value = "Creando perfil de usuario..."
                            
                            // Generar nombre de usuario único
                            val baseUsername = user.email?.substringBefore("@") ?: user.uid.take(8)
                            val uniqueUsername = generateUniqueUsername(baseUsername)
                            
                            val userData = mapOf(
                                "email" to (user.email ?: ""),
                                "nombreCompleto" to (user.displayName ?: ""),
                                "nombreUsuario" to uniqueUsername,
                                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                                "intereses" to listOf<String>(),
                                "fechaRegistro" to com.google.firebase.Timestamp.now()
                            )

                            try {
                                db.collection(RedSocialApp.COLLECTION_USUARIOS)
                                    .document(user.uid)
                                    .set(userData)
                                    .await()
                                
                                _mensaje.value = "¡Perfil creado! Configura tus intereses."
                                _currentUser.value = user
                                _authState.value = AuthState.NeedsIntereses(user)
                            } catch (e: Exception) {
                                val errorMsg = when {
                                    e.message?.contains("permission-denied") == true -> 
                                        "Error de permisos al crear perfil. Verifica las reglas de Firestore."
                                    e.message?.contains("offline") == true -> 
                                        "Error de conexión al crear perfil. La aplicación no puede conectarse a Firestore."
                                    else -> "Error al crear perfil: ${e.message}"
                                }
                                _mensaje.value = errorMsg
                                _authState.value = AuthState.Error(errorMsg)
                            }
                        } else {
                            val intereses = userDoc.get("intereses") as? List<*>
                            _currentUser.value = user
                            if (intereses.isNullOrEmpty()) {
                                _mensaje.value = "Por favor configura tus intereses"
                                _authState.value = AuthState.NeedsIntereses(user)
                            } else {
                                _mensaje.value = "¡Bienvenido de vuelta!"
                                _authState.value = AuthState.Success(user)
                            }
                        }
                    } catch (e: Exception) {
                        val errorMsg = when {
                            e.message?.contains("permission-denied") == true -> 
                                "Error de permisos al verificar perfil. Verifica las reglas de Firestore."
                            e.message?.contains("offline") == true -> 
                                "Error de conexión: La aplicación no puede conectarse a Firestore."
                            else -> "Error al verificar perfil: ${e.message}"
                        }
                        _mensaje.value = errorMsg
                        _authState.value = AuthState.Error(errorMsg)
                    }
                } else {
                    _mensaje.value = "Error: No se pudo obtener información del usuario"
                    _authState.value = AuthState.Error("Error al obtener información del usuario")
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("offline") == true -> 
                        "Error de conexión: La aplicación no puede conectarse a Firebase."
                    e.message?.contains("network") == true -> 
                        "Error de red: Verifica tu conexión"
                    e.message?.contains("canceled") == true -> 
                        "Inicio de sesión cancelado"
                    e.message?.contains("invalid-credential") == true -> 
                        "Credencial inválida. Intenta nuevamente."
                    else -> "Error al iniciar sesión con Google: ${e.message}"
                }
                _mensaje.value = errorMsg
                _authState.value = AuthState.Error(errorMsg)
            }
        }
    }

    fun loginWithGitHub(token: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val credential = GithubAuthProvider.getCredential(token)
                val result = auth.signInWithCredential(credential).await()
                _authState.value = AuthState.Success(result.user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error GitHub")
            }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInAnonymously().await()
                val user = result.user
                
                if (user != null) {
                    val guestData = mapOf(
                        "nombreCompleto" to "Invitado",
                        "nombreUsuario" to "invitado_${user.uid.take(6)}",
                        "email" to "",
                        "fechaRegistro" to com.google.firebase.Timestamp.now(),
                        "photoUrl" to "",
                        "biografia" to "",
                        "esInvitado" to true,
                        "intereses" to listOf<String>()
                    )
                    
                    db.collection("usuarios").document(user.uid)
                        .set(guestData)
                        .await()
                    
                    _authState.value = AuthState.Guest
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al iniciar como invitado")
                _mensaje.value = e.message ?: "Error al iniciar como invitado"
            }
        }
    }

    fun saveInterests(uid: String, intereses: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("usuarios").document(uid)
            .update("intereses", intereses)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al guardar intereses") }
    }

    fun saveIntereses(intereses: List<String>, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            _mensaje.value = "No hay usuario autenticado"
            _authState.value = AuthState.Error("No hay usuario autenticado")
            onComplete(false)
            return
        }

        viewModelScope.launch {
            try {
                _mensaje.value = "Guardando intereses..."
                db.collection("usuarios").document(user.uid)
                    .update("intereses", intereses)
                    .await()
                
                _hasIntereses.value = true
                _mensaje.value = "¡Intereses guardados correctamente!"
                _authState.value = AuthState.Success(user)
                onComplete(true)
            } catch (e: Exception) {
                _mensaje.value = "Error al guardar intereses: ${e.message}"
                _authState.value = AuthState.Error("Error al guardar intereses")
                onComplete(false)
            }
        }
    }

    fun limpiarMensaje() {
        viewModelScope.launch {
            _mensaje.value = null
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
            _currentUser.value = null
            _mensaje.value = null
        }
    }

    companion object {
        const val GITHUB_CLIENT_ID = "Ov23liuuRo44QfJdMfzL"
        private const val GITHUB_CLIENT_SECRET = "c8d8c444b95532ecf5e55dc78de362f7b0800d84"
        const val GITHUB_REDIRECT_URI = "com.example.redsocial://oauth/github"
        private const val GITHUB_SCOPE = "user:email"
    }

    fun getGitHubAuthUrl(): String {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=$GITHUB_CLIENT_ID" +
                "&scope=$GITHUB_SCOPE" +
                "&redirect_uri=$GITHUB_REDIRECT_URI"
    }

    fun handleGitHubCallback(code: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val client = OkHttpClient()
                val requestBody = FormBody.Builder()
                    .add("client_id", GITHUB_CLIENT_ID)
                    .add("client_secret", GITHUB_CLIENT_SECRET)
                    .add("code", code)
                    .build()

                val request = Request.Builder()
                    .url("https://github.com/login/oauth/access_token")
                    .post(requestBody)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    
                    val responseData = response.body?.string() ?: throw IOException("Empty response body")
                    val accessToken = JSONObject(responseData).getString("access_token")
                    
                    val credential = GithubAuthProvider.getCredential(accessToken)
                    val result = auth.signInWithCredential(credential).await()
                    
                    _authState.value = AuthState.Success(result.user)
                    _mensaje.value = "¡Inicio de sesión con GitHub exitoso!"
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error en la autenticación con GitHub")
                _mensaje.value = e.message ?: "Error en la autenticación con GitHub"
            }
        }
    }
} 