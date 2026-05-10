package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName

/**
 * Represents a translation for a show or a movie.
 *
 * @property name Name of the language in the original language
 * @property englishName Name of the language in english
 * @property data Name and overview in the designed language
 */
data class TMDBTranslation(
    @SerializedName("iso_639_1")
    val language: String,
    val name: String,
    @SerializedName("english_name")
    val englishName: String,
    val data: Data
) {

    data class Data(
        val name: String,
        val overview: String
    )

}
