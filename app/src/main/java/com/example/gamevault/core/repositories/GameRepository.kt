package com.example.gamevault.core.repositories

import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.database.WishDao
import com.example.gamevault.core.database.WishEntity
import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.model.GameItem
import com.example.gamevault.core.model.network.ApiClient
import com.example.gamevault.core.model.network.GameService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class GameRepository(
    private val dao: WishDao? = null
) : GameService {

    private val api = ApiClient.gameApi
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    override suspend fun listGames(page: Int, search: String?): ResponseService<List<GameItem>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.listGames(apiKey = ApiClient.API_KEY, page = page, search = search)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) ResponseService.Success(body.results.filter { !it.name.isNullOrBlank() })
                    else ResponseService.Error("Respuesta vacía")
                } else {
                    ResponseService.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                ResponseService.Error("Sin conexión o error inesperado")
            }
        }

    override suspend fun getGameDetail(id: String): ResponseService<GameDetail> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getGame(id = id, apiKey = ApiClient.API_KEY)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) ResponseService.Success(body)
                    else ResponseService.Error("Juego no encontrado")
                } else {
                    ResponseService.Error("Error ${response.code()}")
                }
            } catch (e: Exception) {
                ResponseService.Error("Sin conexión o error inesperado")
            }
        }



    suspend fun addToWishlist(game: GameDetail) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext

        val wishEntity = WishEntity(
            id = game.id,
            userId = uid,
            name = game.name ?: "",
            image = game.backgroundImage ?: "",
            rating = game.rating ?: 0.0
        )


        dao?.insert(wishEntity)


        try {
            firestore.collection("users").document(uid)
                .collection("wishlist").document(game.id.toString())
                .set(wishEntity)
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseError", "Error al guardar en Firestore: ${e.message}")
        }
    }

    suspend fun removeFromWishlist(id: Int) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext

        // 1. Borrar de Room
        dao?.delete(id, uid)


        try {
            firestore.collection("users").document(uid)
                .collection("wishlist").document(id.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseError", "Error al borrar en Firestore: ${e.message}")
        }
    }

    suspend fun isGameSaved(id: Int): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false
        dao?.exists(id, uid) ?: false
    }

    suspend fun getWishlist(): List<WishEntity> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext emptyList()
        dao?.getWishlist(uid) ?: emptyList()
    }
}