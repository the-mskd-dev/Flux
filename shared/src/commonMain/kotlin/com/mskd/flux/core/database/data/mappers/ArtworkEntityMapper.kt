package com.mskd.flux.core.database.data.mappers

import com.mskd.flux.core.database.data.model.ArtworkEntity
import com.mskd.flux.core.model.artwork.Artwork
import kotlin.time.Clock
import kotlin.time.Instant

fun ArtworkEntity.toDomain() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type,
        lastModification = this.lastModification
    )
}

fun Artwork.toEntity() : ArtworkEntity {
    return ArtworkEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type,
        lastModification = Clock.System.now().toEpochMilliseconds()
    )
}