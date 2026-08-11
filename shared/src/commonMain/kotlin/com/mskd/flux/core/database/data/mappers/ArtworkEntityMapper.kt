package com.mskd.flux.core.database.data.mappers

import com.mskd.flux.core.database.data.model.ArtworkEntity
import com.mskd.flux.core.model.artwork.Artwork
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Clock

fun ArtworkEntity.toDomain() : Artwork {
    return Artwork(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type,
        genreIds = this.genreIds.toImmutableList(),
        lastModification = this.lastModification,
    )
}

fun Artwork.toEntity(overrideLastModification: Boolean = true) : ArtworkEntity {
    return ArtworkEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        imagePath = this.imagePath,
        bannerPath = this.bannerPath,
        type = this.type,
        genreIds = this.genreIds,
        lastModification = if (overrideLastModification) Clock.System.now().toEpochMilliseconds() else this.lastModification
    )
}