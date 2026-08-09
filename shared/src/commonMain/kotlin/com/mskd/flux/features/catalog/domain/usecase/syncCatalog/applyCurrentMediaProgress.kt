package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.catalog.Catalog
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase.Companion.TAG
import com.mskd.flux.utils.Trace

/**
 * Copies watch status and current time from existing database media to matched new items.
 */
internal fun applyCurrentMediaProgress(catalog: Catalog, dbMedias: List<Media>) : Catalog {

    var count = 0

    val movies = catalog.movies.map { newMovie ->

        dbMedias.filterIsInstance<Movie>().find { it.file.name == newMovie.file.name && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldMovie ->

            count++

            newMovie.copy(
                currentTime = oldMovie.currentTime,
                status = oldMovie.status
            )

        } ?: newMovie

    }

    val episodes = catalog.episodes.map { newEpisode ->

        dbMedias.filterIsInstance<Episode>().find { it.file.name == newEpisode.file.name && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldEpisode ->

            count++

            newEpisode.copy(
                currentTime = oldEpisode.currentTime,
                status = oldEpisode.status
            )

        } ?: newEpisode

    }

    Trace.info(TAG, "Apply progress on $count new media(s)")

    return Catalog(
        artworks = catalog.artworks,
        movies = movies,
        seasons = catalog.seasons,
        episodes = episodes
    )

}