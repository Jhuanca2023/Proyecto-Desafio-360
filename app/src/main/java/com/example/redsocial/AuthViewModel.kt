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
                            "email" to email
                        )
                        db.collection("usuarios").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    _mensaje.emit("¡Registro exitoso!")
                                    _authState.emit(AuthState.Idle)
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
                .addOnFailureListener {
                    viewModelScope.launch {
                        _authState.emit(AuthState.Error(it.message ?: "Error en el registro"))
                        _mensaje.emit(it.message ?: "Error en el registro")
                        onError?.invoke(it.message ?: "Error en el registro")
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
} 