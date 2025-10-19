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

data class CatalogContent(
    val isLoading: Boolean = true,
    val artworkOverviews: List<ArtworkOverview> = emptyList()
)

class CatalogRepository @Inject constructor(
    private val fileSource: FilesDataSource,
    private val localSource: ArtworkDataSource,
    private val tmdbSource: ArtworkDataSource,
    private val db: FluxDao
) {

    private val _catalogFlow = MutableStateFlow(CatalogContent())
    val catalogFlow: StateFlow<CatalogContent> = _catalogFlow.asStateFlow()

    suspend fun getCatalog(sync: Boolean = false) {

        _catalogFlow.value = _catalogFlow.value.copy(isLoading = true)

        val artworks = if (sync) {
            syncCatalog()
        } else {
            localSource.getArtworks(sync = false).overviews
        }

        // Update content
        _catalogFlow.update { content ->
            content.copy(
                isLoading = false,
                artworkOverviews = artworks.sortedBy { it.title }
            )
        }

    }

    private suspend fun syncCatalog() : List<ArtworkOverview> {

        // Fetch all files, local and online (if possible)
        val allFiles = getFiles()
        val dbFileNames = db.getAllFileNames()

        // Delete artworks with missing files
        db.deleteArtworksWithNoFiles(allFiles)

        // Get new artworks from TMBD
        val newFiles = allFiles.filter { !dbFileNames.contains(it.name) }
        val (newOverviews, newMovies, newEpisodes) = tmdbSource.getArtworks(
            files = newFiles,
            sync = true
        )

        // Save new artworks
        db.insertOverviews(newOverviews)
        db.insertMovies(newMovies)
        db.insertEpisodes(newEpisodes)

        // Return all overviews
        return db.getOverviews()

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
        db.insertMovies(listOf(movie))
    }

    suspend fun saveEpisode(episode: Episode) {
        db.insertEpisodes(listOf(episode))
    }

}