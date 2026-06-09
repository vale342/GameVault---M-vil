package com.example.gamevault.home.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.database.AppDatabase
import com.example.gamevault.core.repositories.GameRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfileData(val fullName: String, val email: String, val phone: String, val wishlistCount: Int)

class AccountViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<ResponseService<UserProfileData>?>(null)
    val uiState: StateFlow<ResponseService<UserProfileData>?> = _uiState.asStateFlow()

    private var wishlistListener: ListenerRegistration? = null

    init {
        activarEscuchaPerfilYWishlist()
    }

    private fun activarEscuchaPerfilYWishlist() {
        val uid = auth.currentUser?.uid ?: return

        // 1. Escuchar cambios en la wishlist en tiempo real
        wishlistListener = firestore.collection("users1")
            .document(uid)
            .collection("wishlist")
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0

                // 2. Obtener datos del perfil cada vez que cambia la wishlist
                viewModelScope.launch {
                    try {
                        val doc = firestore.collection("users1").document(uid).get().await()
                        val nombre = doc.getString("firstName") ?: "Usuario"
                        val email = auth.currentUser?.email ?: ""
                        val phone = doc.getString("phone") ?: "No registrado"

                        _uiState.value = ResponseService.Success(UserProfileData(nombre, email, phone, count))
                    } catch (e: Exception) {
                        // Si falla perfil, mostramos al menos el conteo
                        _uiState.value = ResponseService.Error("Error al actualizar perfil")
                    }
                }
            }
    }

    fun cerrarSesion(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }

    override fun onCleared() {
        super.onCleared()
        wishlistListener?.remove() // Limpiar el listener para evitar fugas de memoria
    }
}