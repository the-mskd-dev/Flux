package com.mskd.flux.features.catalog.domain.fetcher

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.catalog.Catalog
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.features.catalog.domain.resolver.MediaResolver
import com.mskd.flux.report.Trace
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
        val artworkResult = artworkMetadataFetcher.fetch(folders = folders, onProgress = onProgress)
        val artworkFiles = artworkResult.found

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

        // Fichiers en échec: not persisted, retried by the next Synchro légère
        val fetchedSeasons = seasonsAndEpisodes.map { it.first.artworkId to it.first.season }.toSet()
        val fetchedMovies = movies.map { it.artworkId }.toSet()
        val failedFiles = artworkResult.failedFolders.flatMap { it.files } + artworkFiles
            .filterNot { it.artwork.isUnknown }
            .flatMap { (artwork, files) ->
                when (artwork.type) {
                    ContentType.MOVIE -> if (artwork.id in fetchedMovies) emptyList() else files
                    ContentType.SHOW -> files.filter { it.season != null && (artwork.id to it.season) !in fetchedSeasons }
                }
            }

        // An Artwork none of whose files could be fetched is not saved either
        val artworks = artworkFiles.map { it.artwork }.filter { artwork ->
            artwork.isUnknown || artwork.id in fetchedMovies || fetchedSeasons.any { it.first == artwork.id }
        }

        return Catalog(
            artworks = artworks,
            movies = medias.filterIsInstance<Movie>(),
            seasons = seasonsAndEpisodes.map { it.first },
            episodes = medias.filterIsInstance<Episode>(),
            failedFiles = failedFiles
        )

    }

    private companion object { const val TAG = "CatalogContentFetcher" }

}