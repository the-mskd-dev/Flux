package com.mskd.flux.screens.artwork

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.mockups.FakeArtworkRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Status
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: ArtworkViewModel
    lateinit var artworkRepository: FakeArtworkRepository
    lateinit var userRepository: UserRepository
    lateinit var settingsRepository: SettingsRepository

    beforeTest {

        artworkRepository = FakeArtworkRepository(initialContentType = ContentType.SHOW)

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserRepository.State())
        }

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State())
        }

        viewModel = ArtworkViewModel(
            artworkId = MediaMockups.showArtwork.id,
            repository = artworkRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository
        )

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.artwork shouldBe MediaMockups.showArtwork
            initialState.screen shouldBe ScreenState.CONTENT
            initialState.media shouldBe MediaMockups.episode1
            initialState.episodes shouldBe MediaMockups.episodes
            initialState.season shouldBe MediaMockups.episode1.season
            initialState.episodePendingConfirmation shouldBe null

        }

    }

    test("select season") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(ArtworkIntent.SelectSeason(2))

            val updatedState = expectMostRecentItem()

            updatedState.season shouldBe 2

        }

    }

    test("show player") {

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val media = initialState.media

            viewModel.event.test {

                viewModel.handleIntent(ArtworkIntent.PlayMedia(media = media))

                val event = awaitItem()

                event.shouldBeInstanceOf<ArtworkEvent.PlayMedia>()
                event.mediaId shouldBe media.mediaId

            }

        }

    }

    test("show external player") {

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State(externalPlayer = true))
        }

        viewModel = ArtworkViewModel(
            artworkId = MediaMockups.movieArtwork.id,
            repository = artworkRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository
        )

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val media = initialState.media

            viewModel.event.test {

                viewModel.handleIntent(ArtworkIntent.PlayMedia(media = media))

                val event = awaitItem()

                event.shouldBeInstanceOf<ArtworkEvent.LaunchExternalPlayer>()
                event.media shouldBe media

            }

        }

    }

    test("mark first episode as watched") {

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val media = initialState.media

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = media))

            val updatedState = expectMostRecentItem()

            updatedState.media.status shouldBe Status.WATCHED

            cancelAndConsumeRemainingEvents()

        }

    }

    test("request change watch status for second episode with previous") {

        viewModel.uiState.test {

            // Initial state
            expectMostRecentItem()

            // Request change status of episode 2
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))

            // Final state
            val updatedState = expectMostRecentItem()

            updatedState.episodePendingConfirmation shouldBe MediaMockups.episode2

            cancelAndConsumeRemainingEvents()

        }

    }

    test("request change watch status for second episode without previous") {

        viewModel.uiState.test {

            // Initial state
            expectMostRecentItem()

            // Change status of episode 1
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode1))
            awaitItem()

            // Change status of episode 2
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))

            // Final state
            val updatedState = expectMostRecentItem()

            updatedState.episodePendingConfirmation shouldBe null
            updatedState.episodes.find { it.id == MediaMockups.episode1.id }?.status shouldBe Status.WATCHED
            updatedState.episodes.find { it.id == MediaMockups.episode2.id }?.status shouldBe Status.WATCHED

            cancelAndConsumeRemainingEvents()

        }

    }

    test("mark latest episode and previous as watched") {

        viewModel.uiState.test {

            val loadedState = awaitItem()

            // Change status of the latest episode
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = loadedState.episodes.last()))

            val stateWithDialog = expectMostRecentItem()
            stateWithDialog.episodePendingConfirmation shouldNotBe null

            // Validate change for previous episodes
            viewModel.handleIntent(ArtworkIntent.MarkPreviousEpisodesAsWatched)

            // Final state
            val updatedState = expectMostRecentItem()

            updatedState.episodes.all { it.status == Status.WATCHED } shouldBe true

            cancelAndConsumeRemainingEvents()

        }

    }

    test("mark first episode as to watch") {

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()

            // Mark as watched
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = initialState.media))
            val state2 = awaitItem()

            // Mark as not to watch
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = state2.media))


            val finalState = expectMostRecentItem()

            finalState.media.status shouldBe Status.TO_WATCH

            cancelAndConsumeRemainingEvents()

        }

    }

    test("mark movie as watched") {

        artworkRepository.setContent(
            ArtworkRepository.State(
                artwork = MediaMockups.movieArtwork,
                movie = MediaMockups.movie
            )
        )

        viewModel = ArtworkViewModel(
            artworkId = MediaMockups.movieArtwork.id,
            repository = artworkRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository
        )

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()

            initialState.media.status shouldBe Status.TO_WATCH

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = initialState.media))

            val updatedState = expectMostRecentItem()

            updatedState.media.status shouldBe Status.WATCHED

            cancelAndConsumeRemainingEvents()

        }

    }

})