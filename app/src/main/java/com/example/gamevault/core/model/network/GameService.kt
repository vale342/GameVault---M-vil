package com.example.gamevault.core.model.network

import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.model.GameItem

interface GameService {
    suspend fun listGames(page: Int = 1, search: String? = null): ResponseService<List<GameItem>>
    suspend fun getGameDetail(id: String): ResponseService<GameDetail>
}