package com.kaem.flux.screens.artwork

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkViewModelTest : BaseTest() {

    private lateinit var viewModel: ArtworkViewModel
    private lateinit var artworkRepository: ArtworkRepository
    private lateinit var dataStoreRepository: DataStoreRepository

    override fun setUp() {
        super.setUp()

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true) {
            every { this@mockk.get<Any>(any()) } returns ArtworkMockups.showOverview.id
        }

        artworkRepository = mockk(relaxed = true) {
            coEvery { getArtwork(any()) } returns ArtworkRepository.Content(
                artworkOverview = ArtworkMockups.showOverview,
                episodes = ArtworkMockups.episodes
            )
        }

        dataStoreRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(FluxDataStore())
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

    @Test
    fun `mark first episode as watched`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.changeWatchStatus()
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedArtwork?.status == Status.WATCHED)
            coVerify { artworkRepository.saveEpisode(any()) }

        }

    }

    @Test
    fun `request change watch status for second episode with previous`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Select episode 2
            viewModel.selectArtwork(ArtworkMockups.episode2)
            awaitItem()

            // Request change status of current episode
            viewModel.changeWatchStatus()
            advanceUntilIdle()

            // Final state
            val updatedState = awaitItem()

            assert(updatedState.showStatusDialog)

        }

    }

    @Test
    fun `request change watch status for second episode without previous`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Change status of current episode (1)
            viewModel.changeWatchStatus()
            awaitItem()

            // Select episode 2
            viewModel.selectArtwork(ArtworkMockups.episode2)
            awaitItem()

            // Change status of current episode (2)
            viewModel.changeWatchStatus()
            advanceUntilIdle()

            // Final state
            val updatedState = awaitItem()

            assert(!updatedState.showStatusDialog)
            assert(updatedState.episodes.all { it.status == Status.WATCHED })
            coVerify { artworkRepository.saveEpisode(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark second episode and previous as watched`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Select episode 2
            viewModel.selectArtwork(ArtworkMockups.episode2)
            awaitItem()

            // Change status of current and previous episodes
            viewModel.changeWatchStatusForEpisodeAndPrevious()
            advanceUntilIdle()

            // Final state
            val updatedState = awaitItem()

            assert(updatedState.episodes.all { it.status == Status.WATCHED })
            coVerify { artworkRepository.saveEpisode(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark first episode as to watch`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            // Mark as watched
            viewModel.changeWatchStatus()
            awaitItem()

            // Mark as not to watch
            viewModel.changeWatchStatus()
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedArtwork?.status == Status.TO_WATCH)
            coVerify { artworkRepository.saveEpisode(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `save episode progression`() = runTest {

        // Save progression at 5 minutes
        viewModel.saveTime(5.minutes.inWholeMilliseconds)

        advanceUntilIdle()

        coVerify { artworkRepository.saveEpisode(any()) }
        coVerify { dataStoreRepository.addWatchedArtwork(any()) }

    }

    @Test
    fun `end episode watching`() = runTest {

        viewModel.uiState.test {
            awaitItem()

            // Save progression at 5 minutes
            viewModel.saveTime(ArtworkMockups.episode1.duration.minutes.inWholeMilliseconds)
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedArtwork?.status == Status.WATCHED)
            coVerify { artworkRepository.saveEpisode(any()) }
            coVerify { dataStoreRepository.addWatchedArtwork(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark movie as watched`() = runTest {

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true) {
            every { this@mockk.get<Any>(any()) } returns ArtworkMockups.movieOverview.id
        }

        artworkRepository = mockk(relaxed = true) {
            coEvery { getArtwork(any()) } returns ArtworkRepository.Content(
                artworkOverview = ArtworkMockups.movieOverview,
                movie = ArtworkMockups.movie
            )
        }

        viewModel = ArtworkViewModel(savedStateHandle, artworkRepository, dataStoreRepository)

        viewModel.uiState.test {

            awaitItem()

            val initialState = awaitItem()
            assert(initialState.selectedArtwork?.status == Status.TO_WATCH)

            viewModel.changeWatchStatus()
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedArtwork?.status == Status.WATCHED)
            coVerify { artworkRepository.saveMovie(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `end movie watching`() = runTest {

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true) {
            every { this@mockk.get<Any>(any()) } returns ArtworkMockups.movieOverview.id
        }

        artworkRepository = mockk(relaxed = true) {
            coEvery { getArtwork(any()) } returns ArtworkRepository.Content(
                artworkOverview = ArtworkMockups.movieOverview,
                movie = ArtworkMockups.movie
            )
        }

        viewModel = ArtworkViewModel(savedStateHandle, artworkRepository, dataStoreRepository)

        viewModel.uiState.test {

            awaitItem()

            val initialState = awaitItem()
            assert(initialState.selectedArtwork?.status == Status.TO_WATCH)

            viewModel.saveTime(ArtworkMockups.movie.duration.minutes.inWholeMilliseconds)
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedArtwork?.status == Status.WATCHED)
            coVerify { artworkRepository.saveMovie(any()) }
            coVerify { dataStoreRepository.removeWatchedArtwork(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

}