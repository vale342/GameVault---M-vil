package com.example.gamevault.home.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.model.GameItem
import com.example.gamevault.core.model.network.GameService
import com.example.gamevault.core.repositories.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GamesViewModel(
    private val service: GameService = GameRepository()
) : ViewModel() {

    private val _gamesState = MutableStateFlow<ResponseService<List<GameItem>>?>(null)
    val gamesState: StateFlow<ResponseService<List<GameItem>>?> = _gamesState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _gamesState.value = ResponseService.Loading
            _gamesState.value = service.listGames(page = 1)
        }
    }

    fun searchGames(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _gamesState.value = ResponseService.Loading
            _gamesState.value = if (query.isBlank()) {
                service.listGames(page = 1)
            } else {
                service.listGames(page = 1, search = query)
            }
        }
    }
}