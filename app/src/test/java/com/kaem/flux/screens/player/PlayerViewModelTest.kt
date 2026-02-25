package com.kaem.flux.screens.player

import app.cash.turbine.test
import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.screens.artwork.ArtworkIntent
import com.kaem.flux.screens.artwork.ArtworkViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest : BaseTest() {

    //TODO : Unit test

    /*@Test
    fun save_episode_progression() = runTest {

        viewModel.uiState.test {

            // Save progression at 5 minutes
            viewModel.handleIntent(ArtworkIntent.SaveWatchTime(5.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            coVerify { artworkRepository.saveEpisode(any()) }
            coVerify { userRepository.addToRecentlyWatched(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun end_episode_watching() = runTest {

        viewModel.uiState.test {

            awaitItem()

            // Save progression at 5 minutes
            viewModel.handleIntent(ArtworkIntent.SaveWatchTime(MediaMockups.episode1.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            val state = awaitItem()

            assert(state.media.status == Status.WATCHED)
            coVerify { artworkRepository.saveEpisode(any()) }
            coVerify { userRepository.addToRecentlyWatched(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun end_last_episode_watching() = runTest {

        viewModel.uiState.test {
            val initialState = awaitItem()

            // Set first episode as watched
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = initialState.media))
            awaitItem()

            // Play second episode
            viewModel.handleIntent(ArtworkIntent.PlayMedia(media = MediaMockups.episode2))

            // Save progression at the end
            viewModel.handleIntent(ArtworkIntent.SaveWatchTime(MediaMockups.episode2.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            val state = expectMostRecentItem()

            assert(state.media.status == Status.WATCHED)
            coVerify { artworkRepository.saveEpisodes(any()) }
            coVerify { userRepository.removeFromRecentlyWatched(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    @Test
    fun end_movie_watching() = runTest {

        artworkRepository = mockk(relaxed = true) {
            coEvery { getMedia(any()) } returns ArtworkRepository.Content(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
        }

        viewModel = ArtworkViewModel(
            mediaId = MediaMockups.movieArtwork.id,
            repository = artworkRepository,
            settingsRepository = settingsRepository,
            userRepository = userRepository
        )

        viewModel.uiState.test {

            awaitItem()

            var state = awaitItem()
            assert(state.media.status == Status.TO_WATCH)

            viewModel.handleIntent(ArtworkIntent.SaveWatchTime(MediaMockups.movie.duration.minutes.inWholeMilliseconds))

            advanceUntilIdle()

            state = awaitItem()

            assert(state.media.status == Status.WATCHED)
            coVerify { artworkRepository.saveMovie(any()) }
            coVerify { userRepository.removeFromRecentlyWatched(any()) }

            cancelAndConsumeRemainingEvents()

        }

    }*/
}