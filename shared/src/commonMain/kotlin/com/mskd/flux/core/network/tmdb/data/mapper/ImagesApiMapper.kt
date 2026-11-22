package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.language.Language
import com.mskd.flux.core.model.language.languageCode
import com.mskd.flux.core.network.tmdb.data.dto.images.ImageDto
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType

// TMDB can list the same image twice in a row; type + path identifies a Gallery image (it keys the grid, a duplicate crashes it)
fun ImagesDto.toDomain(): List<GalleryImage> =
    (backdrops.map { it.toDomain(GalleryImageType.BACKDROP) } +
        posters.map { it.toDomain(GalleryImageType.POSTER) } +
        logos.pngOnly().map { it.toDomain(GalleryImageType.LOGO) })
        .distinctBy { it.type to it.path }

// PNG only: TMDB does not resize SVG and the image loader does not decode it
private fun List<ImageDto>.pngOnly() = filter { it.filePath.endsWith(".png", ignoreCase = true) }

/**
 * Path of the Logo in [language], then in English, then without language; the best rated within a language.
 * PNG only: TMDB does not resize SVG and the image loader does not decode it. Empty when there is none.
 */
fun ImagesDto.logoPath(language: String): String {
    val candidates = logos.pngOnly()
    return listOf(language.languageCode(), Language.ENGLISH.code, null)
        .firstNotNullOfOrNull { code -> candidates.filter { it.languageCode == code }.maxByOrNull { it.voteAverage } }
        ?.filePath
        .orEmpty()
}

private fun ImageDto.toDomain(type: GalleryImageType) = GalleryImage(
    path = filePath,
    type = type,
    ratio = aspectRatio.toFloat(),
    rating = voteAverage,
    languageCode = languageCode,
)
