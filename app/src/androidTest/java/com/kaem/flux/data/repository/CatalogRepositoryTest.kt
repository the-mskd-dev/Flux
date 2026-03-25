package com.kaem.flux.data.repository

import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.data.repository.catalog.CatalogRepositoryImpl
import com.kaem.flux.data.source.file.FilesSource
import com.kaem.flux.data.source.media.MediaSource
import com.kaem.flux.mockups.MediaMockups
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@MediumTest
class CatalogRepositoryTest {

    private lateinit var repository: CatalogRepository

    private val fileSource: FilesSource = mockk(relaxed = true)
    private val localSource: MediaSource = mockk(relaxed = true)
    private val tmdbSource: MediaSource = mockk(relaxed = true)
    private val db: DatabaseDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        repository = CatalogRepositoryImpl(fileSource, localSource, tmdbSource, db)
    }

    @Test
    fun load_catalog_without_sync() = runTest {
        // Given
        val localMedias = MediaMockups.artworks
        val localMovies = listOf(MediaMockups.movie)
        val localEpisodes = MediaMockups.episodes

        coEvery { localSource.getMedias() } returns MediaSource.Library(
            artworks = localMedias,
            movies = localMovies,
            episodes = localEpisodes
        )

        // When
        repository.loadCatalog(sync = false)

        // Then
        repository.flow.test {

            val loadedState = awaitItem()
            assert(!loadedState.isLoading)
            assert(loadedState.artworks.containsAll(localMedias))

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { localSource.getMedias() }

    }

    @Test
    fun load_catalog_with_sync() = runTest {

        // When
        repository.loadCatalog(sync = true)

        // Then
        repository.flow.test {

            val loadedState = awaitItem()
            assert(!loadedState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { db.getAllFileNames() }
        coVerify { db.deleteMediasWithNoFiles(any()) }
        coVerify { tmdbSource.getMedias(files = any()) }
        coVerify { db.insertArtworks(any()) }
        coVerify { db.insertMovies(any()) }
        coVerify { db.insertEpisodes(any()) }
        coVerify { db.getArtworks() }

    }

}