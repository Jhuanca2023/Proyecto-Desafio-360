package com.example.redsocial

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
import java.io.IOException
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
    object Guest : AuthState()
}

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _authState: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _mensaje: MutableStateFlow<String?> = MutableStateFlow(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    init {
        checkInitialAuthState()
    }

    private fun checkInitialAuthState() {
        viewModelScope.launch {
            auth.currentUser?.let {
                _authState.emit(AuthState.Success(it))
            } ?: _authState.emit(AuthState.Idle)
        }
    }

    private fun resetState() {
        viewModelScope.launch {
            _authState.emit(AuthState.Idle)
            _mensaje.emit(null)
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.emit(AuthState.Loading)
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        _authState.emit(AuthState.Success(auth.currentUser))
                        _mensaje.emit("¡Inicio de sesión exitoso!")
                    }
                }
                .addOnFailureListener {
                    viewModelScope.launch {
                        _authState.emit(AuthState.Error(it.message ?: "Error al iniciar sesión"))
                        _mensaje.emit(it.message ?: "Error al iniciar sesión")
                    }
                }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _authState.emit(AuthState.Loading)
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            _mensaje.emit("Se ha enviado un correo para restablecer tu contraseña")
                            _authState.emit(AuthState.Idle)
                        }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch {
                            _authState.emit(AuthState.Error(e.message ?: "Error al enviar el correo"))
                            _mensaje.emit(e.message ?: "Error al enviar el correo de recuperación")
                        }
                    }
            } catch (e: Exception) {
                _authState.emit(AuthState.Error(e.message ?: "Error inesperado"))
                _mensaje.emit(e.message ?: "Error inesperado")
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
            viewModelScope.launch {
                _mensaje.emit("La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y caracteres especiales")
                _authState.emit(AuthState.Error("Contraseña inválida"))
            }
            return
        }

        if (!validateUsername(nombreUsuario)) {
            viewModelScope.launch {
                _mensaje.emit("El nombre de usuario debe tener al menos 3 caracteres y no puede contener espacios")
                _authState.emit(AuthState.Error("Nombre de usuario inválido"))
            }
            return
        }

        checkUsernameAvailability(nombreUsuario) { isAvailable ->
            if (!isAvailable) {
                viewModelScope.launch {
                    _mensaje.emit("El nombre de usuario ya está en uso")
                    _authState.emit(AuthState.Error("Nombre de usuario no disponible"))
                }
                return@checkUsernameAvailability
            }

            viewModelScope.launch {
                _authState.emit(AuthState.Loading)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            val userData = hashMapOf(
                                "nombreCompleto" to nombreCompleto,
                                "nombreUsuario" to nombreUsuario,
                                "fechaNacimiento" to fechaNacimiento,
                                "genero" to genero,
                                "email" to email,
                                "fechaRegistro" to com.google.firebase.Timestamp.now(),
                                "fotoPerfil" to "",
                                "biografia" to ""
                            )
                            db.collection("usuarios").document(user.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    viewModelScope.launch {
                                        _mensaje.emit("¡Registro exitoso!")
                                        _authState.emit(AuthState.Success(user))
                                        onSuccess?.invoke()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    viewModelScope.launch {
                                        _authState.emit(AuthState.Error(e.message ?: "Error al guardar datos"))
                                        _mensaje.emit(e.message ?: "Error al guardar datos")
                                        onError?.invoke(e.message ?: "Error al guardar datos")
                                    }
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        viewModelScope.launch {
                            _authState.emit(AuthState.Error(e.message ?: "Error en el registro"))
                            _mensaje.emit(e.message ?: "Error en el registro")
                            onError?.invoke(e.message ?: "Error en el registro")
                        }
                    }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.emit(AuthState.Loading)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { 
                    viewModelScope.launch {
                        _authState.emit(AuthState.Success(auth.currentUser))
                    }
                }
                .addOnFailureListener { 
                    viewModelScope.launch {
                        _authState.emit(AuthState.Error(it.message ?: "Error Google"))
                    }
                }
        }
    }

    fun loginWithGitHub(token: String) {
        viewModelScope.launch {
            _authState.emit(AuthState.Loading)
            val credential = GithubAuthProvider.getCredential(token)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { 
                    viewModelScope.launch {
                        _authState.emit(AuthState.Success(auth.currentUser))
                    }
                }
                .addOnFailureListener { 
                    viewModelScope.launch {
                        _authState.emit(AuthState.Error(it.message ?: "Error GitHub"))
                    }
                }
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            _authState.emit(AuthState.Guest)
        }
    }

    fun saveInterests(uid: String, intereses: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("usuarios").document(uid)
            .update("intereses", intereses)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al guardar intereses") }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            resetState()
        }
    }

    fun saveIntereses(intereses: List<String>) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid)
                .update("intereses", intereses)
        }
    }

    fun limpiarMensaje() {
        viewModelScope.launch {
            _mensaje.emit(null)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
            resetState()
            FirebaseAuth.getInstance().signOut() // Aseguramos cerrar sesión en Firebase
        }
    }

    companion object {
        private const val GITHUB_CLIENT_ID = "Ov23liuuRo44QfJdMfzL"
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
            _authState.emit(AuthState.Loading)
            try {
                // Intercambiar el código por un token de acceso
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
                    
                    // Crear credencial de GitHub
                    val credential = GithubAuthProvider.getCredential(accessToken)
                    
                    // Autenticar con Firebase
                    auth.signInWithCredential(credential)
                        .addOnSuccessListener { authResult ->
                            viewModelScope.launch {
                                _authState.emit(AuthState.Success(authResult.user))
                                _mensaje.emit("¡Inicio de sesión con GitHub exitoso!")
                            }
                        }
                        .addOnFailureListener { e ->
                            viewModelScope.launch {
                                _authState.emit(AuthState.Error(e.message ?: "Error en la autenticación con GitHub"))
                                _mensaje.emit(e.message ?: "Error en la autenticación con GitHub")
                            }
                        }
                }
            } catch (e: Exception) {
                _authState.emit(AuthState.Error(e.message ?: "Error en la autenticación con GitHub"))
                _mensaje.emit(e.message ?: "Error en la autenticación con GitHub")
            }
        }
    }
} 