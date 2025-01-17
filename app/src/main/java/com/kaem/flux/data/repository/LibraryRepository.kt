package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val db: FluxDao
) {

    private val _libraryFlow = MutableStateFlow(LibraryContent())
    val libraryFlow: StateFlow<LibraryContent> = _libraryFlow.asStateFlow()

    suspend fun getLibrary(sync: Boolean = false) {

        _libraryFlow.value = _libraryFlow.value.copy(isLoading = true)

        val artworks = if (sync) {
            syncLibrary()
        } else {
            localSource.getArtworks(sync = false).overviews
        }

        // Update content
        _libraryFlow.update { content ->
            content.copy(
                isLoading = false,
                artworkOverviews = artworks.sortedBy { it.title }
            )
        }

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
        db.deleteArtworksWithNoFiles(allFiles)

        // Get new artworks from TMBD
        val (newOverviews, newMovies, newEpisodes) = tmdbSource.getArtworks(
            files = newFiles,
            sync = true
        )

        // Save new artworks
        db.insertOverviews(newOverviews)
        db.insertMovies(newMovies)
        db.insertEpisodes(newEpisodes)

        val filteredOverviews = artworks//.filter { a -> overviewsIdsToDelete.none { it == a.id } }
        return (filteredOverviews + newOverviews).distinctBy { it.id }

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
        return db.insertMovies(listOf(movie))
    }

    suspend fun saveEpisode(episode: Episode) {
        return db.insertEpisodes(listOf(episode))
    }

}