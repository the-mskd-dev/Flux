package com.mskd.flux.core.domain.model.artwork

import com.mskd.flux.features.tmdb.data.dto.MediaTypeDto

enum class ContentType {
    MOVIE,
    SHOW;

    fun equalsTmdb(tmdbType: MediaTypeDto) : Boolean {
        return (this == MOVIE && tmdbType == MediaTypeDto.MOVIE) || (this == SHOW && tmdbType == MediaTypeDto.SHOW)
    }

}