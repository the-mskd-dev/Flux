package com.kaem.flux.screens.player

import app.cash.turbine.test
import com.kaem.flux.configs.fluxExtensions
import com.kaem.flux.data.repository.settings.SettingsPreferences
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.repository.user.UserPreferences
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.mockups.FakeArtworkRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.mockups.PlayerMockups
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.utils.extensions.lastEpisode
import com.kaem.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToLong

class PlayerViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: PlayerViewModel
    lateinit var artworkRepository: FakeArtworkRepository
    lateinit var userRepository: UserRepository
    lateinit var settingsRepository: SettingsRepository

    beforeTest {

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
            ShowSettingsTestCase(
                description = "Show settings sheet",
                sheet = PlayerUiState.SettingsSheet.Settings,
            ),
            ShowSettingsTestCase(
                description = "Show audio sheet",
                sheet = PlayerUiState.SettingsSheet.Tracks(PlayerTrack.Type.AUDIO),
            ),
            ShowSettingsTestCase(
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
            SaveTimeTestCase(
                description = "Movie - save time at the middle",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                time = MediaMockups.movie.duration.minToMs.times(0.5).roundToLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            SaveTimeTestCase(
                description = "Movie - save time at the end",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                time = MediaMockups.movie.duration.minToMs.times(0.95).roundToLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            ),
            SaveTimeTestCase(
                description = "Show - save time at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                time = MediaMockups.episode1.duration.minToMs.times(0.5).roundToLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            SaveTimeTestCase(
                description = "Show - save time at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                time = MediaMockups.episode1.duration.minToMs.times(0.95).roundToLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.WATCHED
            ),
            SaveTimeTestCase(
                description = "Show - save time for last episode at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                time = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.5).roundToLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            SaveTimeTestCase(
                description = "Show - save time for last episode at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                time = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.95).roundToLong(),
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
                settingsRepository = settingsRepository
            )


            viewModel.uiState.test {

                // Skip initial
                awaitItem()

                // When
                viewModel.handleIntent(PlayerIntent.SaveTime(time = testCase.time))

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
            PlayerBackTapTestCase(
                description = "System back tap when interface is showed",
                interfaceShowed = true,
                backSystem = true
            ),
            PlayerBackTapTestCase(
                description = "Button back tap when interface is showed",
                interfaceShowed = true,
                backSystem = false
            ),
            PlayerBackTapTestCase(
                description = "System back tap when interface is not showed",
                interfaceShowed = false,
                backSystem = true
            ),
            PlayerBackTapTestCase(
                description = "Button back tap when interface is not showed",
                interfaceShowed = false,
                backSystem = false
            )
        ) { testCase ->

            viewModel.uiState.test {
                awaitItem()

                if (testCase.interfaceShowed) {
                    viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
                    awaitItem().controls.showInterface shouldBe true
                }

                viewModel.event.test {
                    viewModel.handleIntent(PlayerIntent.OnBackTap(backSystem = testCase.backSystem, time = null))

                    if (testCase.interfaceShowed && testCase.backSystem) {
                        expectNoEvents()
                    } else {
                        awaitItem() shouldBe PlayerEvent.BackToPreviousScreen
                    }
                }

                if (testCase.interfaceShowed && testCase.backSystem) {
                    awaitItem().controls.showInterface shouldBe false
                }

                cancelAndIgnoreRemainingEvents()
            }

        }
    }

    test("toggle play button") {
        viewModel.event.test {
            viewModel.handleIntent(PlayerIntent.TogglePlayButton)
            awaitItem() shouldBe PlayerEvent.TogglePlayButton
        }
    }

    test("set playing status") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(PlayerIntent.SetPlayingStatus(isPlaying = true))
            awaitItem().controls.isPlaying shouldBe true

            viewModel.handleIntent(PlayerIntent.SetPlayingStatus(isPlaying = false))
            awaitItem().controls.isPlaying shouldBe false
        }
    }

    test("fast rewind") {
        viewModel.uiState.test {
            val state = awaitItem()

            viewModel.event.test {
                viewModel.handleIntent(PlayerIntent.OnFastRewind)
                awaitItem() shouldBe PlayerEvent.SeekRewind(state.playerRewind)
            }
        }
    }

    test("fast forward") {
        viewModel.uiState.test {
            val state = awaitItem()

            viewModel.event.test {
                viewModel.handleIntent(PlayerIntent.OnFastForward)
                awaitItem() shouldBe PlayerEvent.SeekForward(state.playerForward)
            }
        }
    }

    test("update progress") {
        viewModel.event.test {
            viewModel.handleIntent(PlayerIntent.UpdateProgress(4L))
            awaitItem().shouldBeInstanceOf<PlayerEvent.UpdateProgress> {
                it.progress shouldBe 4L
            }
        }
    }

    test("update tracks") {
        viewModel.uiState.test {

            awaitItem()

            viewModel.event.test {

                viewModel.handleIntent(PlayerIntent.UpdateTracks(PlayerMockups.tracks))

                val event = awaitItem()
                event.shouldBeInstanceOf<PlayerEvent.SelectTrack>()

            }

            val state = awaitItem()
            state.tracks.tracks shouldBe PlayerMockups.tracks

        }
    }

    context("select track") {

        withData(
            nameFn = { it.description },
            SelectTrackTestCase(
                description = "Select subtitle",
                track = PlayerMockups.Subtitles.english
            ),
            SelectTrackTestCase(
                description = "Select audio",
                track = PlayerMockups.Audio.english
            ),
        ) { testCase ->

            viewModel.uiState.test {

                awaitItem()

                viewModel.event.test {
                    viewModel.handleIntent(PlayerIntent.SelectTrack(testCase.track))

                    val event = awaitItem()
                    event shouldBe PlayerEvent.SelectTrack(testCase.track)
                }

                if (testCase.track.type == PlayerTrack.Type.SUBTITLES) {
                    coVerify { settingsRepository.setSubtitlesLanguage(any()) }
                } else {
                    coVerify { settingsRepository.setAudioLanguage(any()) }
                }

            }

        }

    }

    context("on track selected") {

        withData(
            nameFn = { it.description },
            SelectTrackTestCase(
                description = "Subtitle selected",
                track = PlayerMockups.Subtitles.english
            ),
            SelectTrackTestCase(
                description = "Audio selected",
                track = PlayerMockups.Audio.english
            ),
        ) { testCase ->

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(PlayerIntent.OnTrackSelected(testCase.track))

                val state = awaitItem()

                if (testCase.track.type == PlayerTrack.Type.SUBTITLES)
                    state.tracks.selectedSubtitles shouldBe testCase.track
                else
                    state.tracks.selectedAudio shouldBe testCase.track

            }

        }

    }

    context("show next episode") {
        withData(
            nameFn = { it.description },
            ShowNextEpisodeTestCase(
                description = "Next episode exists",
                currentEpisode = MediaMockups.episode1,
                show = true,
                expectedNexTButton = PlayerUiState.NextButton.Showed(episode = MediaMockups.episode2)
            ),
            ShowNextEpisodeTestCase(
                description = "Next episode doesn't exist",
                currentEpisode = MediaMockups.episodes.lastEpisode,
                show = true,
                expectedNexTButton = PlayerUiState.NextButton.Hidden
            ),
            ShowNextEpisodeTestCase(
                description = "Hide next episode",
                currentEpisode = MediaMockups.episode1,
                show = false,
                expectedNexTButton = PlayerUiState.NextButton.Hidden
            ),
        ) { testCase ->

            artworkRepository.setContentType(ContentType.SHOW)

            viewModel = PlayerViewModel(
                mediaId = testCase.currentEpisode.mediaId,
                artworkRepository = artworkRepository,
                userRepository = userRepository,
                settingsRepository = settingsRepository
            )

            viewModel.uiState.test {

                var state = awaitItem()

                viewModel.handleIntent(PlayerIntent.ShowNextEpisode(show = testCase.show))

                if (state.controls.nextButton != testCase.expectedNexTButton) {
                    state = awaitItem()
                } else {
                    expectNoEvents()
                }

                state.controls.nextButton shouldBe testCase.expectedNexTButton

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
        }
    }

})