package com.kaem.flux.screens.player

import app.cash.turbine.test
import com.kaem.flux.configs.DispatcherConfig
import com.kaem.flux.configs.fluxExtensions
import com.kaem.flux.data.repository.settings.SettingsPreferences
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.repository.user.UserPreferences
import com.kaem.flux.data.repository.user.UserRepository
import com.kaem.flux.mockups.FakeArtworkRepository
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.ContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

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

    test("save time") {
        // TODO
    }

    test("back tap") {
        // TODO
    }

    test("toggle play button") {
        // TODO
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