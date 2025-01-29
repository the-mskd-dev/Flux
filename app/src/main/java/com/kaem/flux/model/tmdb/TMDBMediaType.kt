package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName
import com.kaem.flux.model.tmdb.TMDBMediaType.MOVIE
import com.kaem.flux.model.tmdb.TMDBMediaType.PERSON
import com.kaem.flux.model.tmdb.TMDBMediaType.SHOW


/**
 * Represents the type of media in TMDB.
 *
 * @property SHOW Represents a TV show.
 * @property MOVIE Represents a movie.
 * @property PERSON Represents a person.
 */
enum class TMDBMediaType {
    @SerializedName("tv")
    SHOW,
    @SerializedName("movie")
    MOVIE,
    @SerializedName("person")
    PERSON
}