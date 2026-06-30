package com.mskd.flux.model.domain.artwork

import com.mskd.flux.model.data.remote.tmdb.dto.MediaTypeDto

enum class ContentType {
    MOVIE,
    SHOW;

    fun equalsTmdb(tmdbType: MediaTypeDto) : Boolean {
        return (this == MOVIE && tmdbType == MediaTypeDto.MOVIE) || (this == SHOW && tmdbType == MediaTypeDto.SHOW)
    }

}