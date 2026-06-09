package com.example.gamevault.home.wishlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamevault.core.database.AppDatabase
import com.example.gamevault.core.database.WishEntity
import com.example.gamevault.core.repositories.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WishlistViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repo =
        GameRepository(
            AppDatabase
                .get(application)
                .wishDao()
        )

    private val _games =
        MutableStateFlow<List<WishEntity>>(
            emptyList()
        )

    val games =
        _games.asStateFlow()

    fun loadWishlist() {

        viewModelScope.launch {

            try {

                _games.value =
                    repo.getWishlist()

            } catch (e: Exception) {

                _games.value =
                    emptyList()
            }
        }
    }

    fun deleteGame(
        id: Int
    ) {

        viewModelScope.launch {

            repo.removeFromWishlist(
                id
            )

            loadWishlist()
        }
    }
}