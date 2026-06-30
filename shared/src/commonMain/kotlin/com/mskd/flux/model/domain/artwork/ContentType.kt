package com.mskd.flux.model.domain.artwork

import com.mskd.flux.model.data.remote.tmdb.TMDBMediaType

enum class ContentType {
    MOVIE,
    SHOW;

    fun equalsTmdb(tmdbType: TMDBMediaType) : Boolean {
        return (this == MOVIE && tmdbType == TMDBMediaType.MOVIE) || (this == SHOW && tmdbType == TMDBMediaType.SHOW)
    }

}