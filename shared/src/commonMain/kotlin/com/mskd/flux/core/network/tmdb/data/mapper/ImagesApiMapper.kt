package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.language.Language
import com.mskd.flux.core.model.language.languageCode
import com.mskd.flux.core.network.tmdb.data.dto.images.ImageDto
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType

fun ImagesDto.toDomain(): List<GalleryImage> =
    backdrops.map { it.toDomain(GalleryImageType.BACKDROP) } + posters.map { it.toDomain(GalleryImageType.POSTER) }

/**
 * Path of the Logo in [language], then in English, then without language; the best rated within a language.
 * PNG only: TMDB does not resize SVG and the image loader does not decode it. Empty when there is none.
 */
fun ImagesDto.logoPath(language: String): String {
    val candidates = logos.filter { it.filePath.endsWith(".png", ignoreCase = true) }
    return listOf(language.languageCode(), Language.ENGLISH.code, null)
        .firstNotNullOfOrNull { code -> candidates.filter { it.language == code }.maxByOrNull { it.voteAverage } }
        ?.filePath
        .orEmpty()
}

private fun ImageDto.toDomain(type: GalleryImageType) = GalleryImage(
    path = filePath,
    type = type,
    ratio = aspectRatio.toFloat(),
    rating = voteAverage,
)
