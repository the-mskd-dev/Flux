package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName
import java.util.Locale

data class TMDBTranslations(
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
    data class Translation(
        @SerializedName("iso_639_1")
        val language: String,
        @SerializedName("iso_3166_1")
        val country: String,
        val name: String,
        @SerializedName("english_name")
        val englishName: String,
        val data: Data
    )

    data class Data(
        val name: String?,
        val overview: String?
    )

    sealed class Request(val language: Locale) {
        class Movie(val artworkId: Long, language: Locale) : Request(language)
        class Show(val artworkId: Long, language: Locale) : Request(language)
        class Episode(val artworkId: Long, val season: Int, val number: Int, language: Locale) : Request(language)
        class Season(val artworkId: Long, val season: Int, language: Locale) : Request(language)
    }

}

fun Collection<TMDBTranslations.Translation>.findWithLocale(locale: Locale) : TMDBTranslations.Translation? {
    return this.find { it.language == locale.language && !it.data.overview.isNullOrBlank() }
        ?: this.find { it.language == Locale.ENGLISH.language && !it.data.overview.isNullOrBlank() }
}
