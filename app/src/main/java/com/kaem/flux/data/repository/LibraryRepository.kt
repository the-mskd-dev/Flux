package com.kaem.flux.data.repository

import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.Content
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.FluxMovie
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryContent(
    val isLoading: Boolean = true,
    val artworks: List<Artwork> = emptyList()
)

class LibraryRepository @Inject constructor(
    private val fileSource: FilesDataSource,
    private val localSource: ArtworkDataSource,
    private val tmdbSource: ArtworkDataSource
) {

    private val _libraryContent = MutableStateFlow<LibraryContent?>(null)
    val libraryContent: StateFlow<LibraryContent?> = _libraryContent.asStateFlow()

    suspend fun getLibrary() {

        val allFiles = getFiles()

        val (dbArtworks, dbEpisodes) = localSource.getArtworks(
            files = allFiles
        )

        val dbFiles = dbArtworks.mapNotNull { (it.content as? Content.MOVIE)?.movie?.file?.name } + dbEpisodes.map { it.file.name }
        val filteredFiles =  allFiles.filter { !dbFiles.contains(it.name) }

        val dbIds = dbArtworks.map { it.id } + dbEpisodes.map { it.id }

        val (tmdbArtworks, tmdbEpisodes) = tmdbSource.getArtworks(
            files = filteredFiles,
            artworkIds = dbIds
        )

        val allArtworks = dbArtworks + tmdbArtworks
        val allEpisodes = dbEpisodes + tmdbEpisodes

        allArtworks.forEach { artwork ->

            if (artwork.content is Content.SHOW)
                artwork.content.episodes = allEpisodes.filter { it.showId == artwork.id }.sortedWith(compareBy({ it.season }, { it.number }))

        }

        _libraryContent.value = LibraryContent(
            isLoading = false,
            artworks = allArtworks
        )

    }

    private suspend fun getFiles() : List<UserFile> {

        val localFiles = arrayListOf<UserFile>()

        coroutineScope {

            launch {
                localFiles.addAll(fileSource.getFiles())
            }

            //TODO: Add other sources

        }

        return localFiles

    }

    suspend fun saveArtwork(artwork: Artwork) {
        localSource.saveArtwork(artwork)
    }

    suspend fun saveEpisodes(episodes: List<Episode>) {
        localSource.saveEpisodes(episodes)
    }

}