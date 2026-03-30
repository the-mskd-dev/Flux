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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.collections.listOf

@MediumTest
class CatalogRepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private lateinit var repository: CatalogRepository
    private lateinit var fileSource: FilesSource
    private lateinit var localSource: MediaSource
    private lateinit var tmdbSource: MediaSource
    private lateinit var db: DatabaseDao

    @Before
    fun setUp() {

        db = mockk(relaxed = true)

        fileSource = mockk(relaxed = true)

        tmdbSource = mockk(relaxed = true)

        localSource = mockk(relaxed = true) {
            coEvery { getMedias() } returns MediaSource.Library(
                artworks = MediaMockups.artworks,
                movies = listOf(MediaMockups.movie),
                episodes = MediaMockups.episodes
            )
        }

        repository = CatalogRepositoryImpl(
            fileSource = fileSource,
            mediaSourceLocal = localSource,
            mediaSourceTmdb = tmdbSource,
            db = db,
            scope = testScope
        )

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
        testScope.testScheduler.advanceUntilIdle()

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
        repository.syncCatalog()

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