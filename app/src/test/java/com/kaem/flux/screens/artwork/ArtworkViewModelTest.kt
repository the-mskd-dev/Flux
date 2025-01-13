package com.kaem.flux.screens.artwork

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.FluxDataStore
import com.kaem.flux.mockups.ArtworkMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Status
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Locale

class ArtworkViewModelTest : BaseTest() {

    private lateinit var viewModel: ArtworkViewModel
    private lateinit var artworkRepository: ArtworkRepository
    private lateinit var dataStoreRepository: DataStoreRepository

    private val dataStoreFlow = MutableStateFlow(FluxDataStore())


    override fun setUp() {
        super.setUp()

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true) {
            every { this@mockk.get<Any>(any()) } returns ArtworkMockups.showOverview.id
        }

        artworkRepository = mockk(relaxed = true) {
            coEvery { getArtwork(any()) } returns ArtworkRepository.Content(
                artworkOverview = ArtworkMockups.showOverview,
                movie = null,
                episodes = ArtworkMockups.episodes
            )
        }

        dataStoreRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
            every { getPlayerButtonsValues() } returns Pair(10, 10)
            every { getSubtitlesLanguage() } returns Locale.ENGLISH
        }

        viewModel = ArtworkViewModel(savedStateHandle, artworkRepository, dataStoreRepository)

    }

    @Test
    fun `initial state`() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()

            assert(initialState.overview == ArtworkMockups.showOverview)
            assert(initialState.screen == ScreenState.CONTENT)
            assert(initialState.selectedArtwork == ArtworkMockups.episode1)
            assert(initialState.episodes.size == 2)
            assert(initialState.currentSeason == ArtworkMockups.episode1.season)
            assert(!initialState.showPlayer)
            assert(!initialState.showStatusDialog)

        }

    }

    @Test
    fun `select episode`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.selectArtwork(ArtworkMockups.episode2)
            val updatedState = awaitItem()

            assert(updatedState.selectedArtwork == ArtworkMockups.episode2)

        }

    }

    @Test
    fun `select season`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.selectSeason(2)
            val updatedState = awaitItem()

            assert(updatedState.currentSeason == 2)

        }

    }

    @Test
    fun `show player`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.showPlayer(true)
            val updatedState = awaitItem()

            assert(updatedState.showPlayer)

        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `change watch status for first episode`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.changeWatchStatus()
            advanceUntilIdle()
            val updatedState = awaitItem()

            assert(updatedState.selectedArtwork?.status == Status.WATCHED)
            coVerify { artworkRepository.saveEpisode(any()) }

        }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `change watch status for second episode`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.selectArtwork(ArtworkMockups.episode2)
            viewModel.changeWatchStatus()
            advanceUntilIdle()
            val updatedState = awaitItem()

            //coVerify { viewModel.showStatusDialog() }

        }

    }

}