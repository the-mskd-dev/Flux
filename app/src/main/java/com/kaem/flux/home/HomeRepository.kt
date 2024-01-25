package com.kaem.flux.home

import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource,
    private val tmdbService: TMDBService
) {

    private val artworksMutex = Mutex()
    private val artworks = arrayListOf<FluxArtworkSummary>()
    private val episodesMutex = Mutex()
    private val episodes = arrayListOf<FluxEpisode>()

    suspend fun getArtworks() : Flow<Result<List<FluxArtworkSummary>>> = flow {

        val localFiles = localFilesDataSource.getFiles()

        coroutineScope {

            for (file in localFiles)
                launch { fileToFluxArtwork(file) }

        }

        emit(Result.success(artworks))

    }

    private suspend fun fileToFluxArtwork(file: FileSource): FluxArtwork? {

        val tmdbArtwork = fileToTmdbArtwork(file.nameProperties)

        return tmdbToFluxArtwork(
            tmdbArtwork = tmdbArtwork,
            file = file
        )

    }

    private suspend fun fileToTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBArtwork? {

        return if (fileNameProperties.episode != null && fileNameProperties.season != null) {

            val artworks = tmdbService.getShow(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            artworks.results.maxBy { it.popularity }

        } else {

            val artworks = tmdbService.getMovie(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            artworks.results.firstOrNull()

        }

    }

    private suspend fun tmdbToFluxArtwork(
        tmdbArtwork: TMDBArtwork?,
        file: FileSource
    ) : FluxArtwork? {

        tmdbArtwork ?: return null

        return when (tmdbArtwork.type){

            TMDBMediaType.MOVIE -> {

                val tmdbMovie = tmdbService.getMovieDetails(
                    id = tmdbArtwork.id
                )

                FluxMovie(
                    tmdbMovie = tmdbMovie,
                    file = file,
                )

            }

            TMDBMediaType.SHOW -> {

                val tmdbEpisode = tmdbService.getEpisode(
                    id = tmdbArtwork.id,
                    season = file.nameProperties.season!!,
                    episode = file.nameProperties.episode!!
                )

                FluxEpisode(
                    tmdbEpisode = tmdbEpisode,
                    showId = tmdbArtwork.id,
                    file = file
                )

            }

            else -> null

        }

    }

    private suspend fun addArtworkSummary(artworkSummary: FluxArtworkSummary) {
        artworksMutex.withLock {
            artworks.add(artworkSummary)
        }
    }

    private suspend fun addEpisode(episode: FluxEpisode) {
        episodesMutex.withLock {
            episodes.add(episode)
        }
    }

}