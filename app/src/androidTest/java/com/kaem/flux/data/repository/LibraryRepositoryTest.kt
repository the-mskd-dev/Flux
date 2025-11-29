package com.kaem.flux.data.repository

import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.kaem.flux.data.ddb.DatabaseDao
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
class LibraryRepositoryTest {

    private lateinit var repository: CatalogRepository

    private val fileSource: FilesSource = mockk(relaxed = true)
    private val localSource: MediaSource = mockk(relaxed = true)
    private val tmdbSource: MediaSource = mockk(relaxed = true)
    private val db: DatabaseDao = mockk(relaxed = true)

    @Before
    fun setUp() {
        repository = CatalogRepository(fileSource, localSource, tmdbSource, db)
    }

    @Test
    fun getLibrary_without_sync() = runTest {
        // Given
        val localMedias = MediaMockups.overviews
        val localMovies = listOf(MediaMockups.movie)
        val localEpisodes = MediaMockups.episodes

        coEvery { localSource.getMedias(sync = false) } returns MediaSource.Library(
            overviews = localMedias,
            movies = localMovies,
            episodes = localEpisodes
        )

        // When
        repository.getCatalog(sync = false)

        // Then
        repository.catalogFlow.test {

            val loadedState = awaitItem()
            assert(!loadedState.isLoading)
            assert(loadedState.mediaOverviews.containsAll(localMedias))

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { localSource.getMedias(sync = false) }

    }

    @Test
    fun getLibrary_with_sync() = runTest {

        // When
        repository.getCatalog(sync = true)

        // Then
        repository.catalogFlow.test {

            val loadedState = awaitItem()
            assert(!loadedState.isLoading)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { db.getAllFileNames() }
        coVerify { db.deleteMediasWithNoFiles(any()) }
        coVerify { tmdbSource.getMedias(files = any(), sync = true) }
        coVerify { db.insertOverviews(any()) }
        coVerify { db.insertMovies(any()) }
        coVerify { db.insertEpisodes(any()) }
        coVerify { db.getOverviews() }

    }

}