package com.example.gamevault.core.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class GamesListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<GameItem>
)

@Parcelize
data class GameItem(
    val id: Int,
    val slug: String?,
    val name: String?,
    val released: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    val rating: Double?,
    val metacritic: Int?,
    val playtime: Int?,
    val platforms: List<PlatformWrapper>?,
    val genres: List<Genre>?
) : Parcelable

@Parcelize
data class PlatformWrapper(
    val platform: Platform?
) : Parcelable

@Parcelize
data class Platform(
    val id: Int,
    val name: String?,
    val slug: String?
) : Parcelable

@Parcelize
data class Genre(
    val id: Int,
    val name: String?,
    val slug: String?
) : Parcelable

data class GameDetail(
    val id: Int,
    val slug: String?,
    val name: String?,
    @SerializedName("description_raw") val descriptionRaw: String?,
    val released: String?,
    @SerializedName("background_image") val backgroundImage: String?,
    val website: String?,
    val rating: Double?,
    val metacritic: Int?,
    val playtime: Int?,
    val platforms: List<PlatformWrapper>?,
    val genres: List<Genre>?,
    val developers: List<Developer>?,
    val publishers: List<Publisher>?,
    @SerializedName("esrb_rating") val esrbRating: EsrbRating?
)

data class Developer(val id: Int, val name: String?, val slug: String?)
data class Publisher(val id: Int, val name: String?, val slug: String?)
data class EsrbRating(val id: Int, val name: String?, val slug: String?)