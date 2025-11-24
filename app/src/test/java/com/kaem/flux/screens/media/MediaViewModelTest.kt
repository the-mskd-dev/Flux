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
            assert(initialState.media == MediaMockups.episode1)
            assert(initialState.episodes.size == 3)
            assert(initialState.season == MediaMockups.episode1.season)
            assert(!initialState.showPlayer)
            assert(!initialState.showStatusDialog)

        }

    }

    @Test
    fun `select season`() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(MediaIntent.SelectSeason(2))
            val updatedState = expectMostRecentItem()

            assert(updatedState.season == 2)

        }

    }

    @Test
    fun `show player`() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()
            val media = initialState.media

            viewModel.handleIntent(MediaIntent.PlayMedia(media = media))
            val updatedState = expectMostRecentItem()

            assert(updatedState.showPlayer)

        }

    }

    @Test
    fun `mark first episode as watched`() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()
            val media = initialState.media

            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = media))
            val updatedState = expectMostRecentItem()

            advanceUntilIdle()

            assert(updatedState.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `request change watch status for second episode with previous`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Request change status of episode 2
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = MediaMockups.episode2))
            advanceUntilIdle()

            // Final state
            val updatedState = expectMostRecentItem()

            assert(updatedState.showStatusDialog)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `request change watch status for second episode without previous`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Change status of episode 1
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = MediaMockups.episode1))
            awaitItem()

            // Change status of episode 2
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = MediaMockups.episode2))
            advanceUntilIdle()

            // Final state
            val updatedState = expectMostRecentItem()

            assert(!updatedState.showStatusDialog)
            assert(updatedState.episodes.all { it.status == Status.WATCHED })
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark latest episode and previous as watched`() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Change status of the latest episode
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = MediaMockups.episodes.last()))
            awaitItem()

            // Validate change for previous episodes
            viewModel.handleIntent(MediaIntent.MarkPreviousEpisodesAsWatched)
            advanceUntilIdle()

            // Final state
            val updatedState = expectMostRecentItem()

            assert(updatedState.episodes.all { it.status == Status.WATCHED })
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `mark first episode as to watch`() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()

            // Mark as watched
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = initialState.media))
            val state2 = awaitItem()

            // Mark as not to watch
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = state2.media))
            val finalState = expectMostRecentItem()

            advanceUntilIdle()

            assert(finalState.media.status == Status.TO_WATCH)
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

            assert(state.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisode(any()) }
            coVerify { dataStoreRepository.addWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun `end last episode watching`() = runTest {

        viewModel.uiState.test {
            val initialState = awaitItem()

            // Set first episode as watched
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = initialState.media))
            awaitItem()

            // Play second episode
            viewModel.handleIntent(MediaIntent.PlayMedia(media = MediaMockups.episode2))
            val state = expectMostRecentItem()

            // Save progression at the end
            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.episode2.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            assert(state.media.status == Status.WATCHED)
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
            assert(initialState.media.status == Status.TO_WATCH)

            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = initialState.media))
            val updatedState = expectMostRecentItem()

            advanceUntilIdle()

            assert(updatedState.media.status == Status.WATCHED)
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
            assert(state.media.status == Status.TO_WATCH)

            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.movie.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            assert(state.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveMovie(any()) }
            coVerify { dataStoreRepository.removeWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

}