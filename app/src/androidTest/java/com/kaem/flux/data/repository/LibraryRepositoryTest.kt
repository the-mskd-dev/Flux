package com.kaem.flux.data.repository

import app.cash.turbine.test
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
import com.kaem.flux.mockups.ArtworkMockups
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LibraryRepositoryTest {

    private lateinit var repository: LibraryRepository

    private val fileSource: FilesDataSource = mockk()
    private val localSource: ArtworkDataSource = mockk(relaxed = true)
    private val tmdbSource: ArtworkDataSource = mockk(relaxed = true)
    private val db: FluxDao = mockk()

    @Before
    fun setUp() {
        repository = LibraryRepository(fileSource, localSource, tmdbSource, db)
    }

    @Test
    fun getLibrary_should_update_libraryFlow_with_sorted_artworks() = runTest {
        // Arrange
        val artworks = listOf(
            ArtworkMockups.movieOverview,
            ArtworkMockups.showOverview,
        )
        coEvery { localSource.getArtworks(sync = false) } returns ArtworkDataSource.Library(overviews = artworks)

        // Act
        repository.getLibrary()

        // Assert
        repository.libraryFlow.test {
            val initialLoadingState = awaitItem()
            assertEquals(true, initialLoadingState.isLoading)

            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals(listOf("A", "B"), loadedState.artworkOverviews.map { it.title })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun syncLibrary_should_sync_and_update_libraryFlow() = runTest {
        // Arrange
        val localArtworks = listOf(ArtworkOverview(id = 1, title = "Local"))
        val newArtworks = listOf(ArtworkOverview(id = 2, title = "New"))
        val localFiles = listOf(ArtworkMockups.episode1.file)
        val newFiles = listOf(ArtworkMockups.episode2.file)
        val allFiles = listOf(ArtworkMockups.episode1.file, ArtworkMockups.episode2.file)

        coEvery { localSource.getArtworks(sync = true) } returns ArtworkDataSource.Library(
            overviews = localArtworks,
            movies = emptyList(),
            episodes = emptyList()
        )
        coEvery { fileSource.getFiles() } returns localFiles
        coEvery { tmdbSource.getArtworks(files = newFiles, sync = true) } returns ArtworkDataSource.Library(
            overviews = newArtworks,
            movies = emptyList(),
            episodes = emptyList()
        )

        // Act
        repository.getLibrary(sync = true)

        // Assert
        repository.libraryFlow.test {
            val initialLoadingState = awaitItem()
            assertEquals(true, initialLoadingState.isLoading)

            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals(allFiles.map { it.name }, loadedState.artworkOverviews.map { it.title })
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { db.insertOverviews(newArtworks) }
    }

    @Test
    fun saveMovie_should_insert_movie_into_db() = runTest {
        // Arrange
        val movie = ArtworkMockups.movie
        coEvery { db.insertMovies(listOf(movie)) } returns Unit

        // Act
        repository.saveMovie(movie)

        // Assert
        coVerify { db.insertMovies(listOf(movie)) }
    }

    @Test
    fun saveEpisode_should_insert_episode_into_db() = runTest {
        // Arrange
        val episode = ArtworkMockups.episode1
        coEvery { db.insertEpisodes(listOf(episode)) } returns Unit

        // Act
        repository.saveEpisode(episode)

        // Assert
        coVerify { db.insertEpisodes(listOf(episode)) }
    }
}