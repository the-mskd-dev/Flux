package com.mskd.flux.core.database.data.mappers

import com.mskd.flux.core.database.data.model.GenreEntity
import com.mskd.flux.core.model.artwork.Genre

fun GenreEntity.toDomain() : Genre {
    return Genre(
        id = this.id,
        name = this.name
    )
}

fun Genre.toEntity() : GenreEntity {
    return GenreEntity(
        id = this.id,
        name = this.name,
    )
}