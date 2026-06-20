package com.mskd.flux.shared.model.artwork

import com.mskd.flux.shared.model.tmdb.TMDBMediaType

enum class ContentType {
    MOVIE,
    SHOW;

    fun equalsTmdb(tmdbType: TMDBMediaType) : Boolean {
        return (this == MOVIE && tmdbType == TMDBMediaType.MOVIE) || (this == SHOW && tmdbType == TMDBMediaType.SHOW)
    }

}