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

        // Consultamos la colección 'users' alineada a tus nuevas reglas
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    var firstName = document.getString("firstName") ?: ""
                    var lastName = document.getString("lastName") ?: ""
                    var phone = document.getString("phone") ?: ""

                    // Respaldos por si en tu registro se guardaron con otras minúsculas
                    if (firstName.isEmpty()) firstName = document.getString("firstname") ?: document.getString("name") ?: ""
                    if (lastName.isEmpty()) lastName = document.getString("lastname") ?: ""
                    if (phone.isEmpty()) phone = document.getString("telefono") ?: document.getString("celular") ?: ""

                    val nombreCompleto = "$firstName $lastName".trim()

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

        wishlistListener?.remove()

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
        wishlistListener?.remove()
    }
}