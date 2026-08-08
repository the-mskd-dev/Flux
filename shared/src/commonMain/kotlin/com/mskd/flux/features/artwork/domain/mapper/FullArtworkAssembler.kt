package com.mskd.flux.features.artwork.domain.mapper

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.domain.extension.findForFile
import com.mskd.flux.features.sources.domain.model.UserFolder
import kotlinx.collections.immutable.toImmutableList

internal fun buildFullArtworkMovie(
    artwork: Artwork,
    movie: Movie,
    genres: List<Genre>,
    sources: List<UserFolder>
) : FullArtwork {

    val isAvailable = when (movie.file.source) {
        FileSource.LOCAL -> true
        FileSource.SAF -> sources.findForFile(file = movie.file)?.isAvailable ?: false
    }

    return FullArtwork.FullMovie(
        artwork = artwork,
        movie = movie.copy(isAvailable = isAvailable),
        genres = genres.filter { artwork.genreIds.contains(it.id) }.toImmutableList()
    )
}

internal fun buildFullArtworkShow(
    artwork: Artwork,
    seasons: List<Season>,
    episodes: List<Episode>,
    genres: List<Genre>,
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
        artwork = artwork,
        seasons = seasons.filter { s -> neededSeasons.contains(s.season) }.toImmutableList(),
        episodes = episodesWithAvailability.toImmutableList(),
        genres = genres.filter { artwork.genreIds.contains(it.id) }.toImmutableList()
    )
}