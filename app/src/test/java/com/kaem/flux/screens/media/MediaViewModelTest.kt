package com.kaem.flux.screens.media

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.MediaRepository
import com.kaem.flux.data.repository.SettingsPreferences
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.UserPreferences
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.mockups.MediaMockups
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
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelTest : BaseTest() {

    private lateinit var viewModel: MediaViewModel
    private lateinit var mediaRepository: MediaRepository
    private lateinit var userRepository: UserRepository
    private lateinit var settingsRepository: SettingsRepository

    override fun setUp() {
        super.setUp()

        mediaRepository = mockk(relaxed = true) {
            coEvery { getMedia(any()) } returns MediaRepository.Content(
                mediaOverview = MediaMockups.showOverview,
                episodes = MediaMockups.episodes
            )
        }

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserPreferences())
        }
        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsPreferences())
        }

        viewModel = MediaViewModel(
            mediaId = MediaMockups.showOverview.id,
            repository = mediaRepository,
            settingsRepository = settingsRepository,
            userRepository = userRepository
        )

    }

    @Test
    fun initial_state() = runTest {

        val mediaState = MediaUiState()

        viewModel.uiState.test {

            val initialState = awaitItem()

            assert(initialState.overview == mediaState.overview)
            assert(initialState.screen == mediaState.screen)
            assert(initialState.media == mediaState.media)
            assert(initialState.episodes == mediaState.episodes)
            assert(initialState.season == mediaState.season)
            assert(!initialState.showPlayer)
            assert(initialState.episodePendingConfirmation == null)

        }

    }

    @Test
    fun select_season() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(MediaIntent.SelectSeason(2))

            advanceUntilIdle()

            val updatedState = expectMostRecentItem()

            assert(updatedState.season == 2)

        }

    }

    @Test
    fun show_player() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()
            val media = initialState.media

            viewModel.handleIntent(MediaIntent.PlayMedia(media = media))

            advanceUntilIdle()

            val updatedState = expectMostRecentItem()

            assert(updatedState.showPlayer)

        }

    }

    @Test
    fun mark_first_episode_as_watched() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()
            val media = initialState.media

            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = media))

            advanceUntilIdle()

            val updatedState = expectMostRecentItem()

            assert(updatedState.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun request_change_watch_status_for_second_episode_with_previous() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            // Request change status of episode 2
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = MediaMockups.episode2))

            advanceUntilIdle()

            // Final state
            val updatedState = expectMostRecentItem()

            assert(updatedState.episodePendingConfirmation == MediaMockups.episode2)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun request_change_watch_status_for_second_episode_without_previous() = runTest {

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

            assert(updatedState.episodePendingConfirmation == null)
            assert(updatedState.episodes.all { it.status == Status.WATCHED })
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun mark_latest_episode_and_previous_as_watched() = runTest {

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
    fun mark_first_episode_as_to_watch() = runTest {

        viewModel.uiState.test {

            val initialState = awaitItem()

            // Mark as watched
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = initialState.media))
            val state2 = awaitItem()

            // Mark as not to watch
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = state2.media))

            advanceUntilIdle()

            val finalState = expectMostRecentItem()

            assert(finalState.media.status == Status.TO_WATCH)
            coVerify { mediaRepository.saveEpisodes(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun save_episode_progression() = runTest {

        viewModel.uiState.test {

            // Save progression at 5 minutes
            viewModel.handleIntent(MediaIntent.SaveWatchTime(5.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            coVerify { mediaRepository.saveEpisode(any()) }
            coVerify { userRepository.addWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun end_episode_watching() = runTest {

        viewModel.uiState.test {

            awaitItem()

            // Save progression at 5 minutes
            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.episode1.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            val state = awaitItem()

            assert(state.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisode(any()) }
            coVerify { userRepository.addWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun end_last_episode_watching() = runTest {

        viewModel.uiState.test {
            val initialState = awaitItem()

            // Set first episode as watched
            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = initialState.media))
            awaitItem()

            // Play second episode
            viewModel.handleIntent(MediaIntent.PlayMedia(media = MediaMockups.episode2))

            // Save progression at the end
            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.episode2.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            val state = expectMostRecentItem()

            assert(state.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveEpisodes(any()) }
            coVerify { userRepository.removeWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun mark_movie_as_watched() = runTest {

        mediaRepository = mockk(relaxed = true) {
            coEvery { getMedia(any()) } returns MediaRepository.Content(
                mediaOverview = MediaMockups.movieOverview,
                movie = MediaMockups.movie
            )
        }

        viewModel = MediaViewModel(
            mediaId = MediaMockups.movieOverview.id,
            repository = mediaRepository,
            settingsRepository = settingsRepository,
            userRepository = userRepository
        )

        viewModel.uiState.test {

            awaitItem()

            val initialState = awaitItem()
            assert(initialState.media.status == Status.TO_WATCH)

            viewModel.handleIntent(MediaIntent.ChangeWatchStatus(media = initialState.media))

            advanceUntilIdle()

            val updatedState = expectMostRecentItem()

            assert(updatedState.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveMovie(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun end_movie_watching() = runTest {

        mediaRepository = mockk(relaxed = true) {
            coEvery { getMedia(any()) } returns MediaRepository.Content(
                mediaOverview = MediaMockups.movieOverview,
                movie = MediaMockups.movie
            )
        }

        viewModel = MediaViewModel(
            mediaId = MediaMockups.movieOverview.id,
            repository = mediaRepository,
            settingsRepository = settingsRepository,
            userRepository = userRepository
        )

        viewModel.uiState.test {

            awaitItem()

            var state = awaitItem()
            assert(state.media.status == Status.TO_WATCH)

            viewModel.handleIntent(MediaIntent.SaveWatchTime(MediaMockups.movie.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            state = awaitItem()

            assert(state.media.status == Status.WATCHED)
            coVerify { mediaRepository.saveMovie(any()) }
            coVerify { userRepository.removeWatchedMedia(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

}