package com.kaem.flux.home

import android.util.Log
import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.internal.wait
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource,
    private val tmdbService: TMDBService,
    private val fluxDao: DatabaseDao
) {

    private val databaseMutex = Mutex()
    private val databaseArtworks = arrayListOf<FluxArtworkSummary>()
    private val databaseEpisodes = arrayListOf<FluxEpisode>()

    private val artworksMutex = Mutex()
    private val artworks = arrayListOf<FluxArtworkSummary>()

    private val episodesMutex = Mutex()
    private val episodes = arrayListOf<FluxEpisode>()

    suspend fun getArtworks() : Flow<Result<List<FluxArtworkSummary>>> = flow {

        getFromDatabase()
        val files = getFiles()

        coroutineScope {

            for (file in files)
                launch { fileToFluxArtwork(file) }

        }

        saveInDatabase()

        emit(Result.success(artworks))

    }

    //region Database

    private suspend fun getFromDatabase() {

        coroutineScope {

            launch {

                val movies = fluxDao.getMovies()

                databaseMutex.withLock {
                    databaseArtworks.addAll(movies)
                }

            }

            launch {

                val shows = fluxDao.getShows()

                databaseMutex.withLock {
                    databaseArtworks.addAll(shows)
                }

            }

            launch {

                val ddbEpisodes = fluxDao.getEpisodes()
                databaseEpisodes.addAll(ddbEpisodes)

            }

        }

    }

    private suspend fun saveInDatabase() {

        coroutineScope {

            artworks.forEach {

                launch {

                    when (it) {
                        is FluxMovie -> fluxDao.insertMovie(it)
                        is FluxShow -> fluxDao.insertShow(it)
                    }

                }

            }

            episodes.forEach {

                launch {

                    fluxDao.insertEpisode(it)

                }

            }

        }

    }

    //endregion

    //region Sources

    private suspend fun getFiles() : List<FileSource> {

        val localFiles = arrayListOf<FileSource>()

        coroutineScope {

            launch {
                localFiles.addAll(localFilesDataSource.getFiles())
            }

            //TODO: Add other sources

        }

        return localFiles

    }

    private suspend fun getTMDBArtworks(files: List<FileSource>) : List<TMDBArtwork> {

        var tmdbArtworks: List<TMDBArtwork> = listOf()

        coroutineScope {

            tmdbArtworks = files.map {file ->

                async {

                    getTmdbArtwork(file.nameProperties)

                }

            }
                .awaitAll()
                .filterNotNull()
                .filter { artwork -> databaseArtworks.none { it.id == artwork.id } }

        }

        return tmdbArtworks

    }

    private suspend fun fileToFluxArtwork(file: FileSource) {

        try {

            val tmdbArtwork = getTmdbArtwork(file.nameProperties) ?: return

            getFluxArtwork(
                tmdbArtwork = tmdbArtwork,
                file = file
            )

        } catch (e: Exception) {

            Log.e("HomeRepository", "Fail while fetching artwork", e)

        }

    }

    private suspend fun getTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBArtwork? {

        return if (fileNameProperties.episode != null && fileNameProperties.season != null) {

            val artworks = tmdbService.getShow(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            val artwork = artworks.results.maxBy { it.popularity }
            artwork.type = TMDBMediaType.SHOW

            artwork

        } else {

            val artworks = tmdbService.getMovie(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            val artwork = artworks.results.firstOrNull()
            artwork?.type = TMDBMediaType.MOVIE

            artwork

        }

    }

    private suspend fun getFluxArtwork(
        tmdbArtwork: TMDBArtwork,
        file: FileSource
    ) {

        when (tmdbArtwork.type){

            TMDBMediaType.MOVIE -> {

                if (artworks.any { it.id ==  tmdbArtwork.id})
                    return

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

                if (episodes.any { it.id == tmdbEpisode.id })
                    return

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

    //endregion

    //region Lists

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

    //endregion

}