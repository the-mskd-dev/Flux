package com.mskd.flux.model.data.local.mappers

import com.mskd.flux.model.data.local.entities.ArtworkEntity
import com.mskd.flux.model.domain.artwork.Artwork

fun ArtworkEntity.toDomain() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type
    )
}

fun Artwork.toEntity() : ArtworkEntity {
    return ArtworkEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type
    )
}