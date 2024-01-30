package com.kaem.flux.data.repository

import android.util.Log
import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LibraryContent(
    val artworks: List<FluxArtworkSummary> = emptyList(),
    val episodes: List<FluxEpisode> = emptyList()
)

class LibraryRepository @Inject constructor(
    private val localFilesDataSource: FilesDataSource,
    private val localArtworkDataSource: ArtworkDataSource,
    private val tmdbArtworkDataSource: ArtworkDataSource
) {

    private val _libraryContent = MutableStateFlow(LibraryContent())
    val libraryContent: StateFlow<LibraryContent> = _libraryContent.asStateFlow()

    suspend fun getLibrary() {

        val allFiles = getFiles()

        val (dbArtworks, dbEpisodes) = localArtworkDataSource.getArtworks(
            files = allFiles
        )

        val dbFiles = dbArtworks.filterIsInstance<FluxMovie>().map { it.file.name } + dbEpisodes.map { it.file.name }
        val filteredFiles =  allFiles.filter { !dbFiles.contains(it.name) }

        val dbIds = dbArtworks.map { it.id } + dbEpisodes.map { it.id }

        val (tmdbArtworks, tmdbEpisodes) = tmdbArtworkDataSource.getArtworks(
            files = filteredFiles,
            artworkIds = dbIds
        )

        val dbArtworksIds = dbArtworks.map { it.id }
        val tmdbArtworksIds = tmdbArtworks.map { it.id }

        val dbEpisodesIds = dbEpisodes.map { it.id }
        val tmdbEpisodesIds = tmdbEpisodes.map { it.id }

        Log.d("TEST", "dbArtworksIds : $dbArtworksIds")
        Log.d("TEST", "tmdbArtworksIds : $tmdbArtworksIds")
        Log.d("TEST", "dbEpisodesIds : $dbEpisodesIds")
        Log.d("TEST", "tmdbEpisodesIds : $tmdbEpisodesIds")

        _libraryContent.value = LibraryContent(
            artworks = dbArtworks + tmdbArtworks,
            episodes = dbEpisodes + tmdbEpisodes
        )

    }

    private suspend fun getFiles() : List<UserFile> {

        val localFiles = arrayListOf<UserFile>()

        coroutineScope {

            launch {
                localFiles.addAll(localFilesDataSource.getFiles())
            }

            //TODO: Add other sources

        }

        return localFiles

    }

}