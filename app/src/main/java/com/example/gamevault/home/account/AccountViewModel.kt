package com.example.gamevault.home.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserProfileData(
    val fullName: String,
    val email: String,
    val phone: String,
    val wishlistCount: Int
)

class AccountViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<ResponseService<UserProfileData>?>(null)
    val uiState: StateFlow<ResponseService<UserProfileData>?> = _uiState.asStateFlow()

    private var wishlistListener: ListenerRegistration? = null

    init {
        cargarInformacionPerfil()
    }

    fun cargarInformacionPerfil() {
        val uid = auth.currentUser?.uid ?: return
        val emailReal = auth.currentUser?.email ?: ""

        _uiState.value = ResponseService.Loading

        // Consultar el documento del usuario en Firestore
        firestore.collection("users1").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // 🚀 Adaptabilidad de campos: lee tanto 'firstName' como 'name' para evitar textos en blanco
                    val firstName = document.getString("firstName") ?: document.getString("name") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val phone = document.getString("phone") ?: document.getString("celular") ?: document.getString("telefono") ?: ""

                    // Dar formato al nombre dependiendo de si existen apellidos guardados
                    val nombreCompleto = if (lastName.isNotEmpty()) "$firstName $lastName".trim() else firstName

                    // Proceder a escuchar la colección de favoritos en tiempo real
                    activarEscuchaWishlist(nombreCompleto, emailReal, phone)
                } else {
                    _uiState.value = ResponseService.Error("No se encontró el perfil en la base de datos.")
                }
            }
            .addOnFailureListener { exception ->
                _uiState.value = ResponseService.Error(exception.localizedMessage ?: "Error al conectar con Firestore")
            }
    }

    private fun activarEscuchaWishlist(nombre: String, email: String, telefono: String) {
        val uid = auth.currentUser?.uid ?: return

        // Limpiar cualquier listener previo activo para evitar fugas de memoria
        wishlistListener?.remove()

        // Escucha activa a la colección de favoritos filtrada por el id del usuario
        wishlistListener = firestore.collection("favoritos")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    _uiState.value = ResponseService.Success(
                        UserProfileData(nombre, email, telefono, 0)
                    )
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val count = snapshots.size()
                    _uiState.value = ResponseService.Success(
                        UserProfileData(
                            fullName = if (nombre.isNotEmpty()) nombre else "Usuario",
                            email = email,
                            phone = if (telefono.isNotEmpty()) telefono else "No registrado",
                            wishlistCount = count
                        )
                    )
                }
            }
    }

    fun cerrarSesion(onSuccess: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            onSuccess()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Remover el listener de Firestore al destruir el ciclo de vida del ViewModel
        wishlistListener?.remove()
    }
}