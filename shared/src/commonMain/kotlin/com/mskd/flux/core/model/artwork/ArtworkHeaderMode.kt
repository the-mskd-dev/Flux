package com.mskd.flux.core.model.artwork

/**
 * What the header of an Artwork page shows as Artwork image: the Backdrop, or the Poster with the blurred header.
 * A Movie page and a Show page use the blurred header when the user enabled it; a Season page always shows the Backdrop.
 */
enum class ArtworkHeaderMode {
    NORMAL,
    BLURRED;

    companion object {

        fun of(pageType: ArtworkPageType, blurredHeader: Boolean): ArtworkHeaderMode = when (pageType) {
            ArtworkPageType.MOVIE, ArtworkPageType.SHOW -> if (blurredHeader) BLURRED else NORMAL
            ArtworkPageType.SEASON -> NORMAL
        }

    }
}

/** Backdrop shown for the Artwork: the custom Backdrop when there is one, otherwise the TMDB Backdrop. */
val Artwork.shownBackdropPath: String get() = customBackdropPath ?: bannerPath

/** Poster shown for the Artwork, on its page and in the Catalog: the custom Poster when there is one, otherwise the TMDB Poster. */
val Artwork.shownPosterPath: String get() = customPosterPath ?: imagePath

/** Path of the Artwork image shown by the header in [mode]: the Custom image when there is one, otherwise the TMDB image. */
fun Artwork.artworkImagePath(mode: ArtworkHeaderMode): String = when (mode) {
    ArtworkHeaderMode.NORMAL -> shownBackdropPath
    ArtworkHeaderMode.BLURRED -> shownPosterPath
}
