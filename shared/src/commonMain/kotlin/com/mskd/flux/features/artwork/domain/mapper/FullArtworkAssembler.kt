package com.mskd.flux.features.artwork.domain.mapper

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.domain.extension.findForFile
import com.mskd.flux.features.sources.domain.model.UserFolder

internal fun buildFullArtworkMovie(
    artwork: Artwork,
    movie: Movie,
    sources: List<UserFolder>
) : FullArtwork {

    val isAvailable = when (movie.file.source) {
        FileSource.LOCAL -> true
        FileSource.SAF -> sources.findForFile(file = movie.file)?.isAvailable ?: false
    }

    return FullArtwork.FullMovie(
        resume = artwork,
        movie = movie.copy(isAvailable = isAvailable)
    )
}

internal fun buildFullArtworkShow(
    artwork: Artwork,
    seasons: List<Season>,
    episodes: List<Episode>,
    sources: List<UserFolder>
) : FullArtwork {

    val neededSeasons = episodes.map { it.season }.distinct()

    val episodesWithAvailability = episodes.map { episode ->

        when (episode.file.source) {
            FileSource.LOCAL -> episode
            FileSource.SAF -> {

                episode.copy(
                    isAvailable = sources.findForFile(file = episode.file)?.isAvailable ?: false
                )

            }
        }

    }

    return FullArtwork.FullShow(
        resume = artwork,
        seasons = seasons.filter { s -> neededSeasons.contains(s.season) },
        episodes = episodesWithAvailability
    )
}