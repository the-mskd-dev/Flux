package com.kaem.flux.home

import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private suspend fun fileToFluxArtwork(file: FileSource) {

        val tmdbArtwork = getTmdbArtwork(file.nameProperties) ?: return

        getFluxArtwork(
            tmdbArtwork = tmdbArtwork,
            file = file
        )

    }

    private suspend fun getTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBArtwork? {

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

    private suspend fun getFluxArtwork(
        tmdbArtwork: TMDBArtwork,
        file: FileSource
    ) {

        when (tmdbArtwork.type){

            TMDBMediaType.MOVIE -> {

                val tmdbMovie = tmdbService.getMovieDetails(
                    id = tmdbArtwork.id
                )

                val movie = FluxMovie(
                    tmdbMovie = tmdbMovie,
                    file = file,
                )

                addArtworkSummary(movie)

            }

            TMDBMediaType.SHOW -> {

                if (artworks.none { it.id == tmdbArtwork.id }) {

                    val show = FluxShow(tmdbArtwork = tmdbArtwork)
                    addArtworkSummary(show)

                }

                val tmdbEpisode = tmdbService.getEpisode(
                    id = tmdbArtwork.id,
                    season = file.nameProperties.season!!,
                    episode = file.nameProperties.episode!!
                )

                val episode = FluxEpisode(
                    tmdbEpisode = tmdbEpisode,
                    showId = tmdbArtwork.id,
                    file = file
                )

                addEpisode(episode)

            }

            else -> {}

        }

    }

    private suspend fun addArtworkSummary(artworkSummary: FluxArtworkSummary) {
        artworksMutex.withLock {
            artworks.add(artworkSummary)
        }
    }

    private suspend fun addEpisode(episode: FluxEpisode) {
        episodesMutex.withLock {

            if (episodes.none { it.id == episode.id })
                episodes.add(episode)
        }
    }

}