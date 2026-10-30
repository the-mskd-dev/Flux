package com.mskd.flux.core.network.tmdb.data.dto.show

import com.mskd.flux.core.network.tmdb.data.dto.TranslationsDto
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenreDto
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.core.network.tmdb.data.dto.people.PeopleDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShowDto(
    @SerialName("name")
    val title: String,
    @SerialName("overview")
    val description: String,
    val id: Long,
    @SerialName("poster_path")
    val imagePath: String?,
    @SerialName("backdrop_path")
    val bannerPath: String?,
    @SerialName("vote_average")
    val voteAverage: Float,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("first_air_date")
    val releaseDate: String,
    val genres: List<GenreDto>,
    @SerialName("adult")
    val nsfw: Boolean = false,
    // Appended to the details call (append_to_response=translations,credits,images)
    val translations: TranslationsDto? = null,
    val credits: PeopleDto? = null,
    val images: ImagesDto? = null,
)
