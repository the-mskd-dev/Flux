package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.network.tmdb.data.dto.images.ImageDto
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType

fun ImagesDto.toDomain(): List<GalleryImage> =
    backdrops.map { it.toDomain(GalleryImageType.BACKDROP) } + posters.map { it.toDomain(GalleryImageType.POSTER) }

private fun ImageDto.toDomain(type: GalleryImageType) = GalleryImage(
    path = filePath,
    type = type,
    ratio = aspectRatio.toFloat(),
    rating = voteAverage,
)
