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

    @Query("""
DELETE
FROM wishlist
WHERE id=:id
AND userId=:userId
""")
    suspend fun delete(
        id:Int,
        userId:String
    )

    @Query("""
SELECT EXISTS(
SELECT 1
FROM wishlist
WHERE id=:id
AND userId=:userId
)
""")
    suspend fun exists(
        id:Int,
        userId:String
    ): Boolean

    @Query("""
SELECT *
FROM wishlist
WHERE userId=:userId
""")
    suspend fun getWishlist(
        userId:String
    ): List<WishEntity>

    @Query("SELECT COUNT(*) FROM wishlist WHERE userId=:userId")
    suspend fun getWishlistCount(userId: String): Int
}