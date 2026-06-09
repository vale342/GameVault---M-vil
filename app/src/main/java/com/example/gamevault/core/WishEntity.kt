package com.example.gamevault.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "wishlist"
)
data class WishEntity(

    @PrimaryKey
    val id: Int,

    val userId: String,

    val name: String,

    val image: String,

    val rating: Double
)