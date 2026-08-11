package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.network.tmdb.data.dto.genre.GenreDto

fun GenreDto.toDomain() : Genre {
    return Genre(
        id = this.id,
        name = this.name
    )
}