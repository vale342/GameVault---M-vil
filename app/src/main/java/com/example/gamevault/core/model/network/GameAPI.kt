package com.example.gamevault.core.model.network

import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.model.GamesListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GameAPI {

    @GET("games")
    suspend fun listGames(
        @Query("key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("ordering") ordering: String = "-rating",
        @Query("search") search: String? = null
    ): Response<GamesListResponse>

    @GET("games/{id}")
    suspend fun getGame(
        @Path("id") id: String,
        @Query("key") apiKey: String
    ): Response<GameDetail>
}