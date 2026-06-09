package com.example.gamevault.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WishDao {

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insert(
        game: WishEntity
    )

    @Query(
        "DELETE FROM wishlist WHERE id = :id"
    )
    suspend fun delete(
        id: Int
    )

    @Query(
        "SELECT EXISTS(SELECT 1 FROM wishlist WHERE id=:id)"
    )
    suspend fun exists(
        id: Int
    ): Boolean

    @Query(
        "SELECT * FROM wishlist"
    )
    fun getWishlistFlow(): kotlinx.coroutines.flow.Flow<List<WishEntity>>

    @Query(
        "SELECT * FROM wishlist"
    )
    suspend fun getWishlist():
            List<WishEntity>
}