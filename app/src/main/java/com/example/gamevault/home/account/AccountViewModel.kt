package com.example.gamevault.home.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.database.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 🚀 Instancia segura conectada a la base de datos local de Room (Singleton)
    private val wishDao = AppDatabase.get(application).wishDao()

    private val _uiState = MutableStateFlow<ResponseService<UserProfileData>?>(null)
    val uiState: StateFlow<ResponseService<UserProfileData>?> = _uiState.asStateFlow()

    private var nombreGuardado = ""
    private var emailGuardado = ""
    private var telefonoGuardado = ""

    init {
        cargarInformacionPerfil()
    }

    fun cargarInformacionPerfil() {
        val uid = auth.currentUser?.uid ?: return
        val emailReal = auth.currentUser?.email ?: ""

        _uiState.value = ResponseService.Loading

        firestore.collection("users1").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: document.getString("name") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val phone = document.getString("phone") ?: document.getString("celular") ?: document.getString("telefono") ?: ""

                    val nombreCompleto = if (lastName.isNotEmpty()) "$firstName $lastName".trim() else firstName

                    nombreGuardado = if (nombreCompleto.isNotEmpty()) nombreCompleto else "Usuario"
                    emailGuardado = emailReal
                    telefonoGuardado = if (phone.isNotEmpty()) phone else "No registrado"

                    // 🚀 Activamos la escucha de la base de datos local
                    activarEscuchaWishlistLocal()
                } else {
                    _uiState.value = ResponseService.Error("No se encontró el perfil en la base de datos.")
                }
            }
            .addOnFailureListener { exception ->
                _uiState.value = ResponseService.Error(exception.localizedMessage ?: "Error al conectar con Firestore")
            }
    }

    private fun activarEscuchaWishlistLocal() {
        viewModelScope.launch {
            // 🚀 Conectamos el flujo directo al método de Room
            wishDao.getWishlistFlow().collect { listaDeWishes ->
                val totalJuegos = listaDeWishes.size

                _uiState.value = ResponseService.Success(
                    UserProfileData(
                        fullName = nombreGuardado,
                        email = emailGuardado,
                        phone = telefonoGuardado,
                        wishlistCount = totalJuegos
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
}