package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.catalog.Catalog
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.features.catalog.domain.resolver.MediaResolver
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

interface CatalogContentFetcher {
    suspend fun fetch(folders: List<CatalogFolder>, onProgress: () -> Unit): Catalog
}

class CatalogContentFetcherImpl(
    private val artworkMetadataFetcher: ArtworkMetadataFetcher,
    private val movieMetadataFetcher: MovieMetadataFetcher,
    private val seasonMetadataFetcher: SeasonMetadataFetcher,
    private val mediaResolver: MediaResolver
) : CatalogContentFetcher {

    override suspend fun fetch(folders: List<CatalogFolder>, onProgress: () -> Unit): Catalog {

        // Get artworks
        val artworkFiles = artworkMetadataFetcher.fetch(folders = folders, onProgress = onProgress)

        // Get movies, seasons and episodes
        val (movies, seasonsAndEpisodes) = supervisorScope {
            val moviesDeferred = async {
                runCatching { movieMetadataFetcher.fetch(artworkWithFiles = artworkFiles, onProgress = onProgress) }
                    .onFailure { Trace.error(TAG, "getMovies failed", it) }
                    .getOrElse { emptyList() }
            }
            val seasonsAndEpisodesDeferred = async {
                runCatching { seasonMetadataFetcher.fetch(artworkWithFiles = artworkFiles, onProgress = onProgress) }
                    .onFailure { Trace.error(TAG, "getSeasons failed", it) }
                    .getOrElse { emptyList() }
            }
            moviesDeferred.await() to seasonsAndEpisodesDeferred.await()
        }

        // Create unknown medias
        val unknownMedias = artworkFiles.filter { it.artwork.isUnknown }.flatMap {
            it.files.map { file -> Episode(file) }
        }

        // Resolve duration and translations for Episodes
        val medias = mediaResolver.resolve(
            medias = movies + seasonsAndEpisodes.flatMap { it.second } + unknownMedias,
            onProgress = onProgress
        )

        return Catalog(
            artworks = artworkFiles.map { it.artwork },
            movies = medias.filterIsInstance<Movie>(),
            seasons = seasonsAndEpisodes.map { it.first },
            episodes = medias.filterIsInstance<Episode>()
        )

    }

    private companion object { const val TAG = "CatalogContentFetcher" }

}