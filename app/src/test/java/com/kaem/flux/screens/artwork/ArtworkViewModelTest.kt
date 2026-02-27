package com.kaem.flux.screens.artwork

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.artwork.ArtworkRepository
import com.kaem.flux.data.repository.user.UserPreferences
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.mockups.FakeArtworkRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Status
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkViewModelTest : BaseTest() {

    private lateinit var viewModel: ArtworkViewModel
    private lateinit var artworkRepository: FakeArtworkRepository
    private lateinit var userRepository: UserRepository


    override fun setUp() {
        super.setUp()

        artworkRepository = FakeArtworkRepository(initialContentType = ContentType.SHOW)

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserPreferences())
        }

        viewModel = ArtworkViewModel(
            mediaId = MediaMockups.showArtwork.id,
            repository = artworkRepository,
            userRepository = userRepository
        )

    }

    @Test
    fun initial_state() = runTest {

        val mediaState = ArtworkUiState()

        viewModel.uiState.test {

            val initialState = awaitItem()

            assert(initialState.artwork == mediaState.artwork)
            assert(initialState.screen == mediaState.screen)
            assert(initialState.media == mediaState.media)
            assert(initialState.episodes == mediaState.episodes)
            assert(initialState.season == mediaState.season)
            assert(initialState.episodePendingConfirmation == null)

        }

    }

    @Test
    fun select_season() = runTest {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(ArtworkIntent.SelectSeason(2))

            advanceUntilIdle()

            val updatedState = expectMostRecentItem()

            assert(updatedState.season == 2)

        }

    }

    @Test
    fun show_player() = runTest {

        viewModel.uiState.test {

            advanceUntilIdle()
            val initialState = expectMostRecentItem()
            val media = initialState.media

            viewModel.event.test {

                viewModel.handleIntent(ArtworkIntent.PlayMedia(media = media))

                val event = awaitItem()

                assert(event is ArtworkEvent.PlayMedia)
                assert((event as ArtworkEvent.PlayMedia).mediaId == media.mediaId)

            }

        }

    }

    @Test
    fun mark_first_episode_as_watched() = runTest {

        viewModel.uiState.test {

            advanceUntilIdle()
            val initialState = expectMostRecentItem()
            val media = initialState.media

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = media))

            advanceUntilIdle()

            val updatedState = expectMostRecentItem()

            assert(updatedState.media.status == Status.WATCHED)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun request_change_watch_status_for_second_episode_with_previous() = runTest {

        viewModel.uiState.test {

            // Initial state
            advanceUntilIdle()
            expectMostRecentItem()

            // Request change status of episode 2
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))

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
            advanceUntilIdle()
            expectMostRecentItem()

            // Change status of episode 1
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode1))
            awaitItem()

            // Change status of episode 2
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))

            advanceUntilIdle()

            // Final state
            val updatedState = expectMostRecentItem()

            assert(updatedState.episodePendingConfirmation == null)
            assert(updatedState.episodes.find { it.id == MediaMockups.episode1.id }?.status == Status.WATCHED)
            assert(updatedState.episodes.find { it.id == MediaMockups.episode2.id }?.status == Status.WATCHED)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun mark_latest_episode_and_previous_as_watched() = runTest {

        viewModel.uiState.test {

            // Initial state
            awaitItem()

            val loadedState = awaitItem()

            // Change status of the latest episode
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = loadedState.episodes.last()))

            advanceUntilIdle()

            val stateWithDialog = expectMostRecentItem()
            assert(stateWithDialog.episodePendingConfirmation != null)

            // Validate change for previous episodes
            viewModel.handleIntent(ArtworkIntent.MarkPreviousEpisodesAsWatched)

            advanceUntilIdle()

            // Final state
            val updatedState = expectMostRecentItem()

            assert(updatedState.episodes.all { it.status == Status.WATCHED })

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun mark_first_episode_as_to_watch() = runTest {

        viewModel.uiState.test {

            advanceUntilIdle()
            val initialState = expectMostRecentItem()

            // Mark as watched
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = initialState.media))
            val state2 = awaitItem()

            // Mark as not to watch
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = state2.media))

            advanceUntilIdle()

            val finalState = expectMostRecentItem()

            assert(finalState.media.status == Status.TO_WATCH)

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun mark_movie_as_watched() = runTest {

        artworkRepository.setContent(
            ArtworkRepository.Content(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
        )

        viewModel = ArtworkViewModel(
            mediaId = MediaMockups.movieArtwork.id,
            repository = artworkRepository,
            userRepository = userRepository
        )

        viewModel.uiState.test {

            advanceUntilIdle()
            val initialState = expectMostRecentItem()

            assert(initialState.media.status == Status.TO_WATCH)

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = initialState.media))

            advanceUntilIdle()

            val updatedState = expectMostRecentItem()

            assert(updatedState.media.status == Status.WATCHED)

            cancelAndConsumeRemainingEvents()

        }

    }

}