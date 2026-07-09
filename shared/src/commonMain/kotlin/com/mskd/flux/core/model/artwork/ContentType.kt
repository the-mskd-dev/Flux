package com.mskd.flux.core.model.artwork

import com.mskd.flux.core.network.tmdb.data.remote.dto.MediaTypeDto

enum class ContentType {
    MOVIE,
    SHOW;

    fun equalsTmdb(tmdbType: MediaTypeDto) : Boolean {
        return (this == MOVIE && tmdbType == MediaTypeDto.MOVIE) || (this == SHOW && tmdbType == MediaTypeDto.SHOW)
    }

}