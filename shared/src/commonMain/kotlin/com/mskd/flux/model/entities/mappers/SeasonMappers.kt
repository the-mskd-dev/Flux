package com.mskd.flux.model.entities.mappers

import com.mskd.flux.model.artwork.Season
import com.mskd.flux.model.entities.SeasonEntity
import com.mskd.flux.model.tmdb.TMDBSeason

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

fun TMDBSeason.toDomain(artworkId: Long) : Season {
    return Season(
        id = this.id,
        artworkId = artworkId,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        season = this.season
    )
}