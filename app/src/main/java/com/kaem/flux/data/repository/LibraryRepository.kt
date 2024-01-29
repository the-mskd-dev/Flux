package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LibraryRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource,
    private val tmdbService: TMDBService,
    private val databaseManager: DatabaseManager
) {

    private val dbArtworks = arrayListOf<FluxArtworkSummary>()
    private val dbEpisodes = arrayListOf<FluxEpisode>()

    private val tmdbArtworksMutex = Mutex()
    private val tmdbArtworks = arrayListOf<FluxArtworkSummary>()

    private val tmdbEpisodesMutex = Mutex()
    private val tmdbEpisodes = arrayListOf<FluxEpisode>()

    private val _artworks = MutableStateFlow<List<FluxArtworkSummary>>(emptyList())
    val artworks: StateFlow<List<FluxArtworkSummary>> = _artworks.asStateFlow()

    suspend fun getLibrary() {

        getFromDatabase()

        cleanDatabase()

        getFromTMDB()

        saveInDatabase(
            artworks = tmdbArtworks.toList(),
            episodes = tmdbEpisodes.toList()
        )

        _artworks.value = dbArtworks + tmdbArtworks

    }

    fun getEpisodes() : List<FluxEpisode> {
        return (dbEpisodes + tmdbEpisodes)
    }

    //region Database

    private suspend fun getFromDatabase() {

        databaseManager.getAllArtworks()

        coroutineScope {

            launch {

                val artworks = withContext(Dispatchers.Default) { databaseManager.getAllArtworks() }
                dbArtworks.addAll(artworks)

            }

            launch {

                val episodes = withContext(Dispatchers.Default) { databaseManager.getAllEpisodes() }
                dbEpisodes.addAll(episodes)

            }

        }

    }

    private suspend fun saveInDatabase(
        artworks: List<FluxArtworkSummary>,
        episodes: List<FluxEpisode>
    ) {

        withContext(Dispatchers.Default) {

            launch {

                databaseManager.saveArtworks(artworks)

            }

            launch {

                databaseManager.saveEpisodes(episodes)

            }

        }

    }

    private suspend fun cleanDatabase() {

        val episodesToRemove = arrayListOf<FluxEpisode>()
        val moviesToRemove = arrayListOf<FluxMovie>()
        val showsToRemove = arrayListOf<FluxShow>()

        dbEpisodes.forEach {

            if (!localFilesDataSource.checkIfFileExists(it.file.path))
                episodesToRemove.add(it)

        }

        dbEpisodes.removeAll(episodesToRemove.toSet())

        dbArtworks.filterIsInstance<FluxMovie>().forEach {

            if (!localFilesDataSource.checkIfFileExists(it.file.path))
                moviesToRemove.add(it)

        }

        dbArtworks.filterIsInstance<FluxShow>().forEach { show ->

            if (dbEpisodes.none { it.showId == show.id })
                showsToRemove.add(show)

        }

        dbArtworks.removeAll(showsToRemove.toSet())
        dbArtworks.removeAll(showsToRemove.toSet())

        coroutineScope {

            launch { databaseManager.deleteMovies(moviesToRemove.map { it.id }) }

            launch { databaseManager.deleteShows(showsToRemove.map { it.id }) }

            launch { databaseManager.deleteEpisodes(episodesToRemove.map { it.id }) }

        }


    }

    //endregion

    //region Sources

    private suspend fun getFromTMDB() {

        val files = getFiles()
        getArtworks(files = files)

    }

    private suspend fun getFiles() : List<UserFile> {

        val localFiles = arrayListOf<UserFile>()

        coroutineScope {

            launch {
                localFiles.addAll(localFilesDataSource.getFiles())
            }

            //TODO: Add other sources

        }

        val dbFiles = dbArtworks.filterIsInstance<FluxMovie>().map { it.file.name } + dbEpisodes.map { it.file.name }

        // Return only needed files
        return localFiles.filter { !dbFiles.contains(it.name) }

    }

    private suspend fun getArtworks(files: List<UserFile>) {

        coroutineScope {

            files.forEach { file ->

                launch {

                    val tmdbArtwork = getTmdbArtwork(file.nameProperties)
                    getFluxArtwork(
                        tmdbArtwork = tmdbArtwork,
                        file = file
                    )

                    if (tmdbArtwork?.type == TMDBMediaType.SHOW) {
                        getFluxEpisode(
                            tmdbArtwork = tmdbArtwork,
                            file = file
                        )
                    }

                }

            }

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
        tmdbArtwork: TMDBArtwork?,
        file: UserFile
    ) {

        tmdbArtwork ?: return

        if ((tmdbArtworks + dbArtworks).any { it.id ==  tmdbArtwork.id})
            return

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

                val show = FluxShow(tmdbArtwork = tmdbArtwork)
                addArtworkSummary(show)

            }

            else -> {}

        }

    }

    private suspend fun getFluxEpisode(
        tmdbArtwork: TMDBArtwork?,
        file: UserFile
    ) {

        if (tmdbArtwork == null || (dbEpisodes + tmdbEpisodes).any { it.file.name == file.name })
            return

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

    //endregion

    //region Lists

    private suspend fun addArtworkSummary(artworkSummary: FluxArtworkSummary) {
        tmdbArtworksMutex.withLock {
            tmdbArtworks.add(artworkSummary)
        }
    }

    private suspend fun addEpisode(episode: FluxEpisode) {
        tmdbEpisodesMutex.withLock {
            tmdbEpisodes.add(episode)
        }
    }

    //endregion

}