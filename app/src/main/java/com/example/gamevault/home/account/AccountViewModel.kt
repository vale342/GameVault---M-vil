package com.example.gamevault.home.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.gamevault.onboarding.personal.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // LiveData para exponer el perfil del usuario a la vista
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> get() = _userProfile

    // LiveData para el correo electrónico (proveniente de Auth)
    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String> get() = _userEmail

    // Estado de carga de la pantalla
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _isLoading.value = true
            _userEmail.value = currentUser.email ?: ""

            // Buscamos en la colección "users" el documento con el ID del usuario actual
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Mapeamos el documento al modelo UserProfile que tú creaste
                        val profile = document.toObject(UserProfile::class.java)
                        _userProfile.value = profile
                    }
                    _isLoading.value = false
                }
                .addOnFailureListener {
                    _isLoading.value = false
                    _userProfile.value = null
                }
        }
    }

    fun logout() {
        auth.signOut()
    }
}