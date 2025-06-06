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
import kotlinx.coroutines.launch

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
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje

    init {
        // Persistencia de sesión
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser)
        }
    }

    fun loginWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(auth.currentUser)
                    _mensaje.value = "¡Inicio de sesión exitoso!"
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error de login")
                    _mensaje.value = "Usuario o contraseña incorrectos."
                }
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
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userData = hashMapOf(
                        "nombreCompleto" to nombreCompleto,
                        "nombreUsuario" to nombreUsuario,
                        "email" to email,
                        "fechaNacimiento" to fechaNacimiento,
                        "genero" to genero
                    )
                    if (user != null) {
                        db.collection("usuarios").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success(user)
                                _mensaje.value = "¡Registro exitoso!"
                                onSuccess?.invoke()
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(e.message ?: "Error al guardar usuario")
                                _mensaje.value = "Error al guardar usuario."
                                onError?.invoke(e.message ?: "Error al guardar usuario")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Error de registro")
                    _mensaje.value = task.exception?.message ?: "Error de registro"
                    onError?.invoke(task.exception?.message ?: "Error de registro")
                }
            }
    }

    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { _authState.value = AuthState.Success(auth.currentUser) }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error Google") }
    }

    fun loginWithGitHub(token: String) {
        _authState.value = AuthState.Loading
        val credential = GithubAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { _authState.value = AuthState.Success(auth.currentUser) }
            .addOnFailureListener { _authState.value = AuthState.Error(it.message ?: "Error GitHub") }
    }

    fun loginAsGuest() {
        _authState.value = AuthState.Guest
    }

    fun saveInterests(uid: String, intereses: List<String>, onSuccess: () -> Unit, onError: (String) -> Unit) {
        db.collection("usuarios").document(uid)
            .update("intereses", intereses)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Error al guardar intereses") }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun saveIntereses(intereses: List<String>) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("usuarios").document(user.uid)
                .update("intereses", intereses)
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
        _mensaje.value = "Sesión cerrada."
    }
} 