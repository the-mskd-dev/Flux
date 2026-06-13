package com.mskd.flux.model.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Represents the type of media in TMDB.
 *
 * @property SHOW Represents a TV show.
 * @property MOVIE Represents a movie.
 * @property PERSON Represents a person.
 */
@Serializable
enum class TMDBMediaType {
    @SerialName("tv")
    SHOW,
    @SerialName("movie")
    MOVIE,
    @SerialName("person")
    PERSON
}