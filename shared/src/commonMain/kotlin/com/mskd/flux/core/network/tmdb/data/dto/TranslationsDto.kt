package com.mskd.flux.core.network.tmdb.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranslationsDto(
    val id: String,
    val translations: List<Translation>
) {

    /**
     * Represents a translation for a show or a movie.
     *
     * @property language Language in ISO-639-1
     * @property country Country in ISO-3166-1
     * @property name Name of the language in the original language
     * @property englishName Name of the language in english
     * @property data Name and overview in the designed language
     */
    @Serializable
    data class Translation(
        @SerialName("iso_639_1")
        val language: String,
        @SerialName("iso_3166_1")
        val country: String,
        val name: String,
        @SerialName("english_name")
        val englishName: String,
        val data: Data
    )

    @Serializable
    data class Data(
        val name: String?,
        val overview: String?
    )

}

fun Collection<TranslationsDto.Translation>.findWithLanguage(language: String) : TranslationsDto.Translation? {
    return this.find { it.language.equals(language, true) && !it.data.overview.isNullOrBlank() }
        ?: this.find { it.language.equals("en", true) && !it.data.overview.isNullOrBlank() }
}
