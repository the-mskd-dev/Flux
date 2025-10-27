package com.kaem.flux.screens.media

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.FluxDataStore
import com.kaem.flux.data.repository.MediaRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.Status
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelTest : BaseTest() {

    private lateinit var viewModel: MediaViewModel
    private lateinit var mediaRepository: MediaRepository
    private lateinit var dataStoreRepository: DataStoreRepository

    override fun setUp() {
        super.setUp()

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true) {
            every { this@mockk.get<Any>(any()) } returns MediaMockups.showOverview.id
        }

        mediaRepository = mockk(relaxed = true) {
            coEvery { getMedia(any()) } returns MediaRepository.Content(
                mediaOverview = MediaMockups.showOverview,
                episodes = MediaMockups.episodes
            )
        }

        dataStoreRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(FluxDataStore())
            every { getPlayerButtonsValues() } returns Pair(10, 10)
            every { getSubtitlesLanguage() } returns Locale.ENGLISH
        }

        viewModel = MediaViewModel(savedStateHandle, mediaRepository, dataStoreRepository)

    }

    @Test
    fun `initial state`() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()

            assert(initialState.overview == MediaMockups.showOverview)
            assert(initialState.screen == ScreenState.CONTENT)
            assert(initialState.selectedMedia == MediaMockups.episode1)
            assert(initialState.episodes.size == 2)
            assert(initialState.currentSeason == MediaMockups.episode1.season)
            assert(!initialState.showPlayer)
            assert(!initialState.showStatusDialog)

        }

    }

    @Test
    fun `select episode`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(MediaIntent.SelectEpisode(MediaMockups.episode2))
            val updatedState = awaitItem()

            assert(updatedState.selectedMedia == MediaMockups.episode2)

        }

    }

    @Test
    fun `select season`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(MediaIntent.SelectSeason(2))
            val updatedState = awaitItem()

            assert(updatedState.currentSeason == 2)

        }

    }

    @Test
    fun `show player`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(MediaIntent.ShowPlayer)
            val updatedState = awaitItem()

            assert(updatedState.showPlayer)

        }

    }

    @Test
    fun `mark first episode as watched`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = false))
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedMedia?.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `request change watch status for second episode with previous`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Select episode 2
            viewModel.handleIntent(MediaIntent.SelectEpisode(MediaMockups.episode2))
            awaitItem()

            // Request change status of current episode
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            advanceUntilIdle()

            // Final state
            val updatedState = awaitItem()

            assert(updatedState.showStatusDialog)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `request change watch status for second episode without previous`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Change status of current episode (1)
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            awaitItem()

            // Select episode 2
            viewModel.handleIntent(MediaIntent.SelectEpisode(MediaMockups.episode2))
            awaitItem()

            // Change status of current episode (2)
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            advanceUntilIdle()

            // Final state
            val updatedState = awaitItem()

            assert(!updatedState.showStatusDialog)
            assert(updatedState.episodes.all { it.status == Status.WATCHED })
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark second episode and previous as watched`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Select episode 2
            viewModel.handleIntent(MediaIntent.SelectEpisode(MediaMockups.episode2))
            awaitItem()

            // Change status of current and previous episodes
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            awaitItem()

            // Validate change for previous episodes
            viewModel.handleIntent(MediaIntent.ChangeWatchStatusForEpisodeAndPrevious)
            advanceUntilIdle()

            // Final state
            val updatedState = awaitItem()

            assert(updatedState.episodes.all { it.status == Status.WATCHED })
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark first episode as to watch`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            // Mark as watched
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            awaitItem()

            // Mark as not to watch
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedMedia?.status == Status.TO_WATCH)
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `save episode progression`() = runTest {

        viewModel.uiState.test {

            // Save progression at 5 minutes
            viewModel.handleIntent(MediaIntent.SaveWatchTime(5.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            coVerify { mediaRepository.saveEpisode(any()) }
            coVerify { dataStoreRepository.addWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `end episode watching`() = runTest {

        viewModel.uiState.test {
            val state = awaitItem()

            // Save progression at 5 minutes
            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.episode1.duration.minutes.inWholeMilliseconds))


            advanceUntilIdle()

            assert(state.selectedMedia?.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisode(any()) }
            coVerify { dataStoreRepository.addWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `end last episode watching`() = runTest {

        viewModel.uiState.test {
            awaitItem()

            // Set first episode as watched
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            awaitItem()

            // Select second episode
            viewModel.handleIntent(MediaIntent.SelectEpisode(MediaMockups.episode2))
            val state = awaitItem()

            // Save progression at the end
            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.episode2.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            assert(state.selectedMedia?.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisodes(any()) }
            coVerify { dataStoreRepository.removeWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark movie as watched`() = runTest {

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true) {
            every { this@mockk.get<Any>(any()) } returns MediaMockups.movieOverview.id
        }

        mediaRepository = mockk(relaxed = true) {
            coEvery { getMedia(any()) } returns MediaRepository.Content(
                mediaOverview = MediaMockups.movieOverview,
                movie = MediaMockups.movie
            )
        }

        viewModel = MediaViewModel(savedStateHandle, mediaRepository, dataStoreRepository)

        viewModel.uiState.test {

            awaitItem()

            val initialState = awaitItem()
            assert(initialState.selectedMedia?.status == Status.TO_WATCH)

            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true))
            val updatedState = awaitItem()

            advanceUntilIdle()

            assert(updatedState.selectedMedia?.status == Status.WATCHED)
            coVerify { mediaRepository.saveMovie(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `end movie watching`() = runTest {

        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true) {
            every { this@mockk.get<Any>(any()) } returns MediaMockups.movieOverview.id
        }

        mediaRepository = mockk(relaxed = true) {
            coEvery { getMedia(any()) } returns MediaRepository.Content(
                mediaOverview = MediaMockups.movieOverview,
                movie = MediaMockups.movie
            )
        }

        viewModel = MediaViewModel(savedStateHandle, mediaRepository, dataStoreRepository)

        viewModel.uiState.test {

            awaitItem()

            val state = awaitItem()
            assert(state.selectedMedia?.status == Status.TO_WATCH)

            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.movie.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            assert(state.selectedMedia?.status == Status.WATCHED)
            coVerify { mediaRepository.saveMovie(any()) }
            coVerify { dataStoreRepository.removeWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

}