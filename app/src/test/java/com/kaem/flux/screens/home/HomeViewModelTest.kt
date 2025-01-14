package com.kaem.flux.screens.home

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.FluxDataStore
import com.kaem.flux.data.repository.LibraryContent
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.mockups.ArtworkMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.ArtworkOverview
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : BaseTest() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var libraryRepository: LibraryRepository
    private lateinit var dataStoreRepository: DataStoreRepository

    // Mocked flows
    private val libraryFlow = MutableStateFlow(LibraryContent())
    private val dataStoreFlow = MutableStateFlow(FluxDataStore())

    override fun setUp() {
        super.setUp()

        libraryRepository = mockk(relaxed = true) {
            every { libraryFlow } returns this@HomeViewModelTest.libraryFlow
        }

        dataStoreRepository = mockk(relaxed = true) {
            every { flow } returns this@HomeViewModelTest.dataStoreFlow
            every { getSyncTime() } returns 0L
        }

        viewModel = HomeViewModel(libraryRepository, dataStoreRepository)
    }

    @Test
    fun `initial state`() = runTest {

        viewModel.uiState.test {
            val initialState = awaitItem()
            assert(ScreenState.LOADING == initialState.screenState)
            assert(emptyList<ArtworkOverview>() == initialState.overviews)
            assert(emptyList<Long>() == initialState.lastWatchedArtworkIds)
            assert(initialState.isSyncing)

            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun `combine flows should update state`() = runTest {

        // Mock
        val overviews = listOf(ArtworkMockups.movieOverview, ArtworkMockups.showOverview)
        val lastWatchedIds = listOf(ArtworkMockups.showOverview.id)
        val libraryContent = LibraryContent(
            isLoading = false,
            artworkOverviews = overviews
        )
        val dataStore = FluxDataStore(
            watchedIds = lastWatchedIds
        )

        viewModel.uiState.test {
            awaitItem() // Ignore initial state

            libraryFlow.value = libraryContent
            dataStoreFlow.value = dataStore

            val updatedState = awaitItem()

            assert(ScreenState.CONTENT == updatedState.screenState)
            assert(overviews == updatedState.overviews)
            assert(lastWatchedIds == updatedState.lastWatchedArtworkIds)
            assert(!updatedState.isSyncing)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `should force sync when manual sync requested`() = runTest {
        viewModel.getLibrary(manualSync = true)

        advanceUntilIdle()

        coVerify {
            libraryRepository.getLibrary(sync = true)
            dataStoreRepository.saveSyncTime(any())
        }
    }


    @Test
    fun `should sync when last sync was more than 1 day ago`() = runTest {
        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        every { dataStoreRepository.getSyncTime() } returns oldTime

        viewModel = HomeViewModel(libraryRepository, dataStoreRepository)
        viewModel.getLibrary(manualSync = false)

        advanceUntilIdle()

        coVerify {
            libraryRepository.getLibrary(sync = true)
            dataStoreRepository.saveSyncTime(any())
        }
    }

    @Test
    fun `should not sync when last sync was less than 1 day ago`() = runTest {
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        every { dataStoreRepository.getSyncTime() } returns recentTime

        viewModel = HomeViewModel(libraryRepository, dataStoreRepository)
        viewModel.getLibrary(manualSync = false)

        advanceUntilIdle()

        coVerify {
            libraryRepository.getLibrary(sync = false)
        }
        coVerify(exactly = 0) {
            dataStoreRepository.saveSyncTime(any())
        }
    }

}