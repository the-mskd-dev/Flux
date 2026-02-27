package com.kaem.flux.screens.player

import com.kaem.flux.bases.BaseTest
import com.kaem.flux.data.repository.artwork.ArtworkRepository
import com.kaem.flux.data.repository.settings.SettingsPreferences
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.repository.user.UserPreferences
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.mockups.FakeArtworkRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.ContentType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest : BaseTest() {

    private lateinit var viewModel: PlayerViewModel

    private lateinit var artworkRepository: FakeArtworkRepository

    private lateinit var userRepository: UserRepository

    private lateinit var settingsRepository: SettingsRepository

    override fun setUp() {
        super.setUp()

        artworkRepository = FakeArtworkRepository(initialContentType = ContentType.SHOW)

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserPreferences())
        }

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsPreferences())
        }

        viewModel = PlayerViewModel(
            mediaId = MediaMockups.episode1.mediaId,
            artworkRepository = artworkRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository
        )
    }

    @Test
    fun show_interface() = runTest {

    }

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