package com.example.gamevault.core.repositories

import android.util.Log
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.database.WishDao
import com.example.gamevault.core.database.WishEntity
import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.model.GameItem
import com.example.gamevault.core.model.network.ApiClient
import com.example.gamevault.core.model.network.GameService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GameRepository(private val dao: WishDao? = null) : GameService {

    private val api = ApiClient.gameApi
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun listGames(page: Int, search: String?): ResponseService<List<GameItem>> = withContext(Dispatchers.IO) {
        try {
            val response = api.listGames(apiKey = ApiClient.API_KEY, page = page, search = search)
            if (response.isSuccessful) {
                ResponseService.Success(response.body()?.results?.filter { !it.name.isNullOrBlank() } ?: emptyList())
            } else {
                ResponseService.Error("Error ${response.code()}")
            }
        } catch (e: Exception) {
            ResponseService.Error("Sin conexión")
        }
    }

    override suspend fun getGameDetail(id: String) = withContext(Dispatchers.IO) {
        try {
            val response = api.getGame(id = id, apiKey = ApiClient.API_KEY)
            if (response.isSuccessful) ResponseService.Success(response.body()!!)
            else ResponseService.Error("Error ${response.code()}")
        } catch (e: Exception) {
            ResponseService.Error("Sin conexión")
        }
    }

    suspend fun addToWishlist(game: GameDetail) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val entity = WishEntity(game.id, uid, game.name ?: "", game.backgroundImage ?: "", game.rating ?: 0.0)

        dao?.insert(entity)
        try {
            firestore.collection("users1").document(uid).collection("wishlist").document(game.id.toString()).set(entity).await()
        } catch (e: Exception) {
            Log.e("Firebase", "Error al guardar: ${e.message}")
        }
    }

    suspend fun removeFromWishlist(id: Int) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        dao?.delete(id, uid)
        try {
            firestore.collection("users1").document(uid).collection("wishlist").document(id.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("Firebase", "Error al borrar: ${e.message}")
        }
    }

    suspend fun getWishlist(): List<WishEntity> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        dao?.getWishlist(uid) ?: emptyList()
    }

    suspend fun getWishlistCount(): Int = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext 0
        return@withContext try {
            firestore.collection("users1").document(uid).collection("wishlist").get().await().size()
        } catch (e: Exception) {
            dao?.getWishlistCount(uid) ?: 0
        }
    }


    suspend fun isGameSaved(id: Int): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false

        dao?.exists(id, uid) ?: false
    }
}