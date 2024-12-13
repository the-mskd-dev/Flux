package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.ArtworkOverview
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryContent(
    val isLoading: Boolean = true,
    val artworkOverviews: List<ArtworkOverview> = emptyList()
)

class LibraryRepository @Inject constructor(
    private val fileSource: FilesDataSource,
    private val localSource: ArtworkDataSource,
    private val tmdbSource: ArtworkDataSource,
    private val databaseManager: DatabaseManager
) {

    private val _libraryContent = MutableStateFlow<LibraryContent?>(null)
    val libraryContent: StateFlow<LibraryContent?> = _libraryContent.asStateFlow()

    suspend fun getLibrary(sync: Boolean = false) {

        val artworks = if (sync) {
            syncLibrary()
        } else {
            localSource.getArtworks(sync = false).artworkOverviews
        }

        // Update content
        _libraryContent.value = LibraryContent(
            isLoading = false,
            artworkOverviews = artworks.sortedBy { it.title }
        )

    }

    private suspend fun syncLibrary() : List<ArtworkOverview> {

        // Get all artworks
        val (artworks, movies, episodes) = localSource.getArtworks(sync = true)

        // Fetch all files, local and online (if possible)
        val allFiles = getFiles()

        // Filter files absents of Artworks in the DB
        val savedFiles = movies.map { it.file } + episodes.map { it.file }
        val newFiles = allFiles.filter { f -> savedFiles.none { it.name == f.name } }

        // Delete artworks with missing files
        val moviesToDelete = movies.filter { m -> allFiles.none { it.name == m.file.name } }
        val episodesToDelete = episodes.filter { e -> allFiles.none { it.name == e.file.name } }
        val artworksToDelete = artworks.filter { artwork -> moviesToDelete.any { it.artworkId == artwork.id } || episodesToDelete.containsAll(episodes.filter { it.artworkId == artwork.id }) }
        databaseManager.deleteArtworks(artworksToDelete.map { it.id })
        databaseManager.deleteEpisodes(episodesToDelete.map { it.id })

        // Get new artworks from TMBD
        val filteredArtwork = artworks.filter { a -> artworksToDelete.none { it.id == a.id } }
        val (newArtworks, newMovies, newEpisodes) = tmdbSource.getArtworks(
            files = newFiles,
            artworkIds = filteredArtwork.map { it.id },
            sync = true
        )

        // Save new artworks
        databaseManager.saveArtworks(newArtworks)
        databaseManager.saveMovies(newMovies)
        databaseManager.saveEpisodes(newEpisodes)

        return filteredArtwork + newArtworks

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

    suspend fun saveMovie(movie: Movie) {
        return databaseManager.saveMovies(listOf(movie))
    }

    suspend fun saveEpisode(episode: Episode) {
        return databaseManager.saveEpisodes(listOf(episode))
    }

}