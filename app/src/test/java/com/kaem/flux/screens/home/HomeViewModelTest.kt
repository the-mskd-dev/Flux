package com.kaem.flux.screens.home

import app.cash.turbine.test
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.FluxDataStore
import com.kaem.flux.data.repository.LibraryContent
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.mockups.ArtworkMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.ArtworkOverview
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.prefs.Preferences

class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val repository: LibraryRepository = mockk()
    private val dataStoreRepository: DataStoreRepository = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {

        Dispatchers.setMain(Dispatchers.Unconfined)

        // Setup mocks
        every { dataStoreRepository.getSyncTime() } returns 0L
        every { dataStoreRepository.flow } returns flowOf(mockk())
        val libraryFlow = MutableStateFlow(mockk<LibraryContent>())
        coEvery { repository.libraryFlow } returns libraryFlow

        // Init ViewModel
        viewModel = HomeViewModel(repository, dataStoreRepository)

    }

    @Test
    fun initial_state() = runTest {

        viewModel.uiState.test {
            val initialState = awaitItem()
            assert(ScreenState.LOADING == initialState.screenState)
            assert(emptyList<ArtworkOverview>() == initialState.overviews)
            assert(emptyList<Long>() == initialState.lastWatchedArtworkIds)
            assert(initialState.isSyncing)
        }

    }

    @Test
    fun combine_flows_should_update_state() = runTest {

        // Mock
        val overviews = listOf(ArtworkMockups.movieOverview, ArtworkMockups.showOverview)
        val lastWatchedIds = listOf(ArtworkMockups.showOverview.id)
        val libraryContent = mockk<LibraryContent> {
            every { isLoading } returns false
            every { artworkOverviews } returns overviews
        }
        val dataStore = mockk<FluxDataStore> {
            every { lastWatchedIds } returns lastWatchedIds
        }

        // Mock flow
        val mockedLibraryFlow = MutableStateFlow(libraryContent)
        coEvery { repository.libraryFlow } returns mockedLibraryFlow
        every { dataStoreRepository.flow } returns flowOf(dataStore)


        viewModel.uiState.test {
            val initialState = awaitItem() // Ignore initial state
            val updatedState = awaitItem()

            assert(ScreenState.CONTENT == updatedState.screenState)
            assert(overviews == updatedState.overviews)
            assert(lastWatchedIds == updatedState.lastWatchedArtworkIds)
            assert(!updatedState.isSyncing)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.shutdown()
    }

}