package com.mskd.flux.core.database.mappers

import com.mskd.flux.model.data.local.entities.SeasonEntity
import com.mskd.flux.core.domain.model.artwork.Season

fun SeasonEntity.toDomain() : Season {
    return Season(
        id = this.id,
        artworkId = this.artworkId,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        season = this.season
    )
}

fun Season.toEntity() : SeasonEntity {
    return SeasonEntity(
        id = this.id,
        artworkId = this.artworkId,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        season = this.season
    )
}