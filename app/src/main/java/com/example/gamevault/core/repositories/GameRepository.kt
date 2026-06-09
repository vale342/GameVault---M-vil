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

class GameRepository(
    private val dao: WishDao? = null
) : GameService {

    private val api = ApiClient.gameApi

    override suspend fun listGames(
        page: Int,
        search: String?
    ): ResponseService<List<GameItem>> =
        withContext(Dispatchers.IO) {

            try {

                val response =
                    api.listGames(
                        apiKey = ApiClient.API_KEY,
                        page = page,
                        search = search
                    )

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null) {

                        ResponseService.Success(
                            body.results.filter {
                                !it.name.isNullOrBlank()
                            }
                        )

                    } else {

                        ResponseService.Error(
                            "Respuesta vacía"
                        )
                    }

                } else {

                    when (response.code()) {

                        401 ->
                            ResponseService.Error(
                                "API key inválida"
                            )

                        429 ->
                            ResponseService.Error(
                                "Demasiadas peticiones"
                            )

                        else ->
                            ResponseService.Error(
                                "Error ${response.code()}"
                            )
                    }
                }

            } catch (_: Exception) {

                ResponseService.Error(
                    "Sin conexión o error inesperado"
                )
            }
        }

    override suspend fun getGameDetail(
        id: String
    ): ResponseService<GameDetail> =
        withContext(Dispatchers.IO) {

            try {

                val response =
                    api.getGame(
                        id = id,
                        apiKey = ApiClient.API_KEY
                    )

                if (response.isSuccessful) {

                    val body = response.body()

                    if (body != null) {
                        ResponseService.Success(body)
                    } else {
                        ResponseService.Error(
                            "Juego no encontrado"
                        )
                    }

                } else {

                    when (response.code()) {

                        404 ->
                            ResponseService.Error(
                                "Ese juego no existe"
                            )

                        401 ->
                            ResponseService.Error(
                                "API key inválida"
                            )

                        429 ->
                            ResponseService.Error(
                                "Demasiadas peticiones"
                            )

                        else ->
                            ResponseService.Error(
                                "Error ${response.code()}"
                            )
                    }
                }

            } catch (_: Exception) {

                ResponseService.Error(
                    "Sin conexión o error inesperado"
                )
            }
        }

    suspend fun addToWishlist(
        game: GameDetail
    ) {

        dao?.insert(
            WishEntity(
                id = game.id,
                name = game.name ?: "",
                image = game.backgroundImage ?: "",
                rating = game.rating ?: 0.0
            )
        )
    }

    suspend fun removeFromWishlist(
        id: Int
    ) {

        dao?.delete(id)
    }

    suspend fun isGameSaved(
        id: Int
    ): Boolean {

        return dao?.exists(id) ?: false
    }

    suspend fun getWishlist(): List<WishEntity> {

        return dao?.getWishlist()
            ?: emptyList()
    }
}