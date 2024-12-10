package com.kaem.flux.data.repository

import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkType
import com.kaem.flux.model.flux.Episode
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

        // Fetch all files, local and online (if possible)
        val allFiles = getFiles()

        // Fetch all artworks infos already in the DB according to the files
        val (dbArtworks, dbEpisodes) = localSource.getArtworks(
            files = allFiles
        )

        // Filter files absents of Artworks in the DB
        val dbFiles = dbArtworks.mapNotNull { (it.type as? ArtworkType.MOVIE)?.movie?.file?.name } + dbEpisodes.map { it.file.name }
        val filteredFiles =  allFiles.filter { !dbFiles.contains(it.name) }
        val dbIds = dbArtworks.map { it.id } + dbEpisodes.map { it.id }

        // Fetch all new artworks infos from TMDB with files
        val (tmdbArtworks, tmdbEpisodes) = tmdbSource.getArtworks(
            files = filteredFiles,
            artworkIds = dbIds
        )

        // Merge all artworks infos from DB and TMDB
        val allArtworks = dbArtworks + tmdbArtworks
        val allEpisodes = dbEpisodes + tmdbEpisodes

        // Sort episode for shows
        allArtworks.forEach { artwork ->

            if (artwork.type is ArtworkType.SHOW)
                artwork.type.episodes = allEpisodes.filter { it.showId == artwork.id }.sortedWith(compareBy({ it.season }, { it.number }))

        }

        // Update content
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