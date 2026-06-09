package com.example.gamevault.core.repositories

import com.example.gamevault.core.ResponseService
import com.example.gamevault.onboarding.personal.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository : UserService {
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("users1")

    override suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit> = withContext(Dispatchers.IO) {
        try {

            userCollection.document(userProfile.id)
                .set(userProfile)
                .await()

            ResponseService.Success(Unit)
        } catch (e: Exception) {

            ResponseService.Error("Error al guardar perfil: ${e.message ?: "Desconocido"}")
        }
    }


    suspend fun userExists(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = userCollection.document(userId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }
}