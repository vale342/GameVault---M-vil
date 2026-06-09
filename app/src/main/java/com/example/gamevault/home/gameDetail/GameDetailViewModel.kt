package com.example.gamevault.home.gameDetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.ResponseService
import com.example.gamevault.core.database.AppDatabase
import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.repositories.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameDetailViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repo =
        GameRepository(
            AppDatabase
                .get(application)
                .wishDao()
        )

    private val _isSaved =
        MutableStateFlow(false)

    val isSaved =
        _isSaved.asStateFlow()

    private val _detailState =
        MutableStateFlow<ResponseService<GameDetail>?>(null)

    val detailState =
        _detailState.asStateFlow()

    fun fetchGameDetail(id: String) {

        viewModelScope.launch {

            _detailState.value =
                ResponseService.Loading

            val result =
                repo.getGameDetail(id)

            _detailState.value =
                result

            if (
                result is ResponseService.Success &&
                result.data != null
            ) {

                _isSaved.value =
                    repo.isGameSaved(
                        result.data.id
                    )
            }
        }
    }

    fun toggleWishlist(
        game: GameDetail
    ) {

        viewModelScope.launch {

            if (_isSaved.value) {

                repo.removeFromWishlist(
                    game.id
                )

            } else {

                repo.addToWishlist(
                    game
                )
            }

            _isSaved.value =
                !_isSaved.value
        }
    }
}