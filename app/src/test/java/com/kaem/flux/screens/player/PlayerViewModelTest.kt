package com.kaem.flux.screens.player

import app.cash.turbine.test
import com.kaem.flux.configs.fluxExtensions
import com.kaem.flux.data.repository.settings.SettingsPreferences
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.repository.user.UserPreferences
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.mockups.FakeArtworkRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.utils.extensions.lastEpisode
import com.kaem.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToLong

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: PlayerViewModel
    lateinit var artworkRepository: FakeArtworkRepository
    var userRepository: UserRepository = mockk(relaxed = true)
    var settingsRepository: SettingsRepository = mockk(relaxed = true)

    beforeTest {

        artworkRepository = FakeArtworkRepository(initialContentType = ContentType.SHOW)

        every { userRepository.flow } returns MutableStateFlow(UserPreferences())

        every { settingsRepository.flow } returns MutableStateFlow(SettingsPreferences())

        viewModel = PlayerViewModel(
            mediaId = MediaMockups.episode1.mediaId,
            artworkRepository = artworkRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository
        )

    }

    test("show interface") {
        viewModel.uiState.test {

            // Hidden by default
            val initialState = awaitItem()
            initialState.controls.showInterface shouldBe false

            // Test show
            viewModel.handleIntent(PlayerIntent.ShowInterface)
            val showedState = awaitItem()
            showedState.controls.showInterface shouldBe true

            // Test hide
            viewModel.handleIntent(PlayerIntent.ShowInterface)
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
                description = "Back tap when interface is showed",
                interfaceShowed = true
            ),
            PlayerBackTapTestCase(
                description = "Back tap when interface is not showed",
                interfaceShowed = false
            )
        ) { testCase ->

            viewModel.uiState.test {
                awaitItem()

                if (testCase.interfaceShowed) {
                    viewModel.handleIntent(PlayerIntent.ShowInterface)
                    awaitItem().controls.showInterface shouldBe true
                }

                viewModel.event.test {
                    viewModel.handleIntent(PlayerIntent.OnBackTap(time = null))

                    if (testCase.interfaceShowed) {
                        expectNoEvents()
                    } else {
                        awaitItem() shouldBe PlayerEvent.BackToPreviousScreen
                    }
                }

                if (testCase.interfaceShowed) {
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
        // TODO
    }

    test("fast rewind") {
        // TODO
    }

    test("fast forward") {
        // TODO
    }

    test("update progress") {
        // TODO
    }

    test("update tracks") {
        // TODO
    }

    test("select track") {
        // TODO
    }

    test("on track selected") {
        // TODO
    }

    test("show next episode") {
        // TODO
    }

    test("cancel next episode") {
        // TODO
    }

    test("play next episode") {
        //TODO
    }

})