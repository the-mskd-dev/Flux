package com.mskd.flux.features.artwork.domain.mapper

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season

internal fun buildFullArtworkMovie(artwork: Artwork, movie: Movie) : FullArtwork {
    return FullArtwork.FullMovie(
        resume = artwork,
        movie = movie
    )
}

internal fun buildFullArtworkShow(artwork: Artwork, seasons: List<Season>, episodes: List<Episode>) : FullArtwork {

    val availableSeasons = seasons.map { it.season }
    val neededSeasons = episodes.map { it.season }.distinct()

    return FullArtwork.FullShow(
        resume = artwork,
        seasons = seasons.filter { s -> neededSeasons.contains(s.season) },
        episodes = if (artwork.isUnknown) episodes else episodes.filter { e -> availableSeasons.contains(e.season) }
    )
}