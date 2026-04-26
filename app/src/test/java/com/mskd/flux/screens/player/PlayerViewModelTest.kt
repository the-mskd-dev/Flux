package com.mskd.flux.screens.player

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.mockups.FakeArtworkRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PlayerMockups
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.screens.player.controllers.PlayerManager
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.seconds

class PlayerViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: PlayerViewModel
    lateinit var artworkRepository: FakeArtworkRepository
    lateinit var userRepository: UserRepository
    lateinit var settingsRepository: SettingsRepository
    lateinit var playerManager: PlayerManager

    beforeTest {

        artworkRepository = FakeArtworkRepository(initialContentType = ContentType.SHOW)

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserRepository.State())
        }

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State())
        }

        playerManager = mockk(relaxed = true) {
            every { state } returns MutableStateFlow(PlayerManager.State())
        }

        viewModel = PlayerViewModel(
            mediaId = MediaMockups.episode1.mediaId,
            artworkRepository = artworkRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository,
            playerManager = playerManager
        )

    }

    test("interface visibility") {
        viewModel.uiState.test {

            // Hidden by default
            val initialState = awaitItem()
            initialState.controls.showInterface shouldBe false

            // Test show
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
            val showedState = awaitItem()
            showedState.controls.showInterface shouldBe true

            // Test hide
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
            val hiddenState = awaitItem()
            hiddenState.controls.showInterface shouldBe false

        }
    }

    context("show settings") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.ShowSettings(
                description = "Show settings sheet",
                sheet = PlayerUiState.SettingsSheet.Settings,
            ),
            PlayerTestCases.ShowSettings(
                description = "Show audio sheet",
                sheet = PlayerUiState.SettingsSheet.Tracks(PlayerTrack.Type.AUDIO),
            ),
            PlayerTestCases.ShowSettings(
                description = "Show subtitles sheet",
                sheet = PlayerUiState.SettingsSheet.Settings,
            )
        ) { testCase ->

            viewModel.uiState.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.ShowSettings(sheet = testCase.sheet))

                // Then
                val settingsState = awaitItem()
                settingsState.controls.settingsSheet shouldBe testCase.sheet

            }

        }
    }

    context("save time") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.SaveTime(
                description = "Movie - save time at the middle",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                time = MediaMockups.movie.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            PlayerTestCases.SaveTime(
                description = "Movie - save time at the end",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                time = MediaMockups.movie.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                time = MediaMockups.episode1.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                time = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.WATCHED
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time for last episode at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                time = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            PlayerTestCases.SaveTime(
                description = "Show - save time for last episode at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                time = MediaMockups.episodes.lastEpisode.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            )
        ) { testCase ->

            // Given
            artworkRepository.setContentType(if (testCase.media is Movie) ContentType.MOVIE else ContentType.SHOW)

            viewModel = PlayerViewModel(
                mediaId = testCase.media.mediaId,
                artworkRepository = artworkRepository,
                userRepository = userRepository,
                settingsRepository = settingsRepository,
                playerManager = playerManager
            )


            viewModel.uiState.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.SaveTime)

                // Then
                val state = awaitItem()
                state.media.shouldNotBeNull {
                    status shouldBe testCase.statusExpected
                }
                if (testCase.shouldBeAddedToRecentlyWatched) {
                    coVerify { userRepository.addToRecentlyWatched(testCase.artwork.id) }
                } else {
                    coVerify { userRepository.removeFromRecentlyWatched(testCase.artwork.id) }
                }

            }


        }
    }

    context("back tap") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.PlayerBackTap(
                description = "Back tap when interface is showed",
                interfaceShowed = true,
            ),
            PlayerTestCases.PlayerBackTap(
                description = "Back tap when interface is not showed",
                interfaceShowed = false,
            )
        ) { testCase ->

            viewModel.uiState.test {
                awaitItem()

                if (testCase.interfaceShowed) {
                    viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
                    awaitItem().controls.showInterface shouldBe true
                }

                viewModel.event.test {
                    viewModel.handleIntent(PlayerIntent.OnBackTap)

                    if (testCase.interfaceShowed) {
                        awaitItem() shouldBe PlayerEvent.BackToPreviousScreen
                    } else {
                        expectNoEvents()
                    }
                }

                cancelAndIgnoreRemainingEvents()
            }

        }
    }

    test("toggle play button") {
        viewModel.event.test {
            viewModel.handleIntent(PlayerIntent.TogglePlayButton)
            coVerify { playerManager.togglePlay() }
        }
    }

    test("fast rewind") {
        viewModel.uiState.test {
            val state = awaitItem()

            viewModel.handleIntent(PlayerIntent.OnFastRewind)

            val finalState = awaitItem()
            finalState.seekOverlay shouldBe PlayerUiState.SeekOverlay(amount = state.playerForward, type = PlayerUiState.SeekOverlay.Type.REWIND)
            coVerify { playerManager.seekRewind(any()) }
        }
    }

    test("fast forward") {
        viewModel.uiState.test {
            val state = awaitItem()

            viewModel.handleIntent(PlayerIntent.OnFastForward)

            val finalState = awaitItem()
            finalState.seekOverlay shouldBe PlayerUiState.SeekOverlay(amount = state.playerForward, type = PlayerUiState.SeekOverlay.Type.FORWARD)
            coVerify { playerManager.seekForward(any()) }

        }
    }

    test("update progress") {
        viewModel.event.test {
            viewModel.handleIntent(PlayerIntent.UpdateProgress(4L))

            coVerify { playerManager.seekTo(progress = 4L) }
        }
    }

    context("select track") {

        withData(
            nameFn = { it.description },
            PlayerTestCases.SelectTrack(
                description = "Select subtitle",
                track = PlayerMockups.Subtitles.english
            ),
            PlayerTestCases.SelectTrack(
                description = "Select audio",
                track = PlayerMockups.Audio.english
            ),
        ) { testCase ->

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(PlayerIntent.SelectTrack(testCase.track))

                awaitItem()

                coVerify { playerManager.selectTrack(track = testCase.track) }
                if (testCase.track.type == PlayerTrack.Type.SUBTITLES) {
                    coVerify { settingsRepository.setSubtitlesLanguage(any()) }
                } else {
                    coVerify { settingsRepository.setAudioLanguage(any()) }
                }

            }

        }

    }

    test("cancel next episode") {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.CancelNextEpisode)

            val state = awaitItem()
            state.controls.nextButton.shouldBeInstanceOf<PlayerUiState.NextButton.Canceled>()

        }

    }

    test("play next episode") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.PlayNextEpisode(MediaMockups.episode2))

            val state = awaitItem()
            state.media.shouldNotBeNull {
                mediaId shouldBe MediaMockups.episode2.mediaId
            }
            coVerify { artworkRepository.saveEpisode(any()) }
        }
    }

    test("on volume change") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.OnVolumeChange(delta = .5f))

            val state = awaitItem()
            state.ambientOverlay shouldBe PlayerUiState.AmbientOverlay(type = PlayerUiState.AmbientOverlay.Type.VOLUME, value = 50)
            coVerify { playerManager.changeVolume(.5f) }

        }
    }

    test("on brightness change") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.event.test {

                viewModel.handleIntent(PlayerIntent.OnBrightnessChange(delta = .5f))

                val event = awaitItem()

                event shouldBe PlayerEvent.ChangeBrightness(delta = .5f)

            }

        }
    }

    context("update ambient overlay") {
        withData(
            nameFn = { it.description },
            PlayerTestCases.UpdateAmbientOverlay(
                description = "Brightness",
                type = PlayerUiState.AmbientOverlay.Type.BRIGHTNESS,
                value = 50
            ),
            PlayerTestCases.UpdateAmbientOverlay(
                description = "Volume",
                type = PlayerUiState.AmbientOverlay.Type.VOLUME,
                value = 50
            )
        ) { testCase ->
            viewModel.uiState.test {
                awaitItem()

                viewModel.handleIntent(PlayerIntent.UpdateAmbientOverlay(type = testCase.type, value = testCase.value))

                var state = awaitItem()
                state.ambientOverlay shouldBe PlayerUiState.AmbientOverlay(type = testCase.type, value = testCase.value)
            }
        }
    }

})