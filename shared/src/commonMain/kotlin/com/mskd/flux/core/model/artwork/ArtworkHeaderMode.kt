package com.mskd.flux.core.model.artwork

/**
 * What the header of an Artwork page shows as Artwork image: the Backdrop, or the Poster with the blurred header.
 * Only a Movie page has a blurred header.
 */
enum class ArtworkHeaderMode {
    NORMAL,
    BLURRED;

    companion object {

        fun of(type: ContentType, blurredHeader: Boolean): ArtworkHeaderMode =
            if (type == ContentType.MOVIE && blurredHeader) BLURRED else NORMAL

    }
}

/**
 * Path of the Artwork image shown by the header in [mode]: the Image personnalisée when there is one, otherwise the TMDB image.
 * The single rule shared by the page header and the Gallery.
 */
fun Artwork.artworkImagePath(mode: ArtworkHeaderMode): String = when (mode) {
    ArtworkHeaderMode.NORMAL -> customBackdropPath ?: bannerPath
    // The custom Poster is not chosen yet: the blurred header keeps the TMDB Poster
    ArtworkHeaderMode.BLURRED -> imagePath
}
