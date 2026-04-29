package com.mskd.flux.screens.unknown

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.mockups.FakeArtworkRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.ScreenState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class UnknownViewModelTest : FunSpec ({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var artworkRepository: FakeArtworkRepository
    lateinit var settingsRepository: SettingsRepository

    beforeTest {

        artworkRepository = FakeArtworkRepository()

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State())
        }

        viewModel = UnknownViewModel(
            repository = artworkRepository,
            settingsRepository = settingsRepository
        )

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.medias shouldBe MediaMockups.unknowns
            initialState.screen shouldBe ScreenState.CONTENT

        }

    }

    test("play media") {
        viewModel.event.test {


            viewModel.handleIntent(UnknownIntent.PlayMedia(media = MediaMockups.unknownEpisode))
            val event = awaitItem()

            event shouldBe UnknownEvent.PlayMedia(MediaMockups.unknownEpisode.id)

        }
    }

    test("play media - external player") {

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State(externalPlayer = true))
        }

        viewModel = UnknownViewModel(
            repository = artworkRepository,
            settingsRepository = settingsRepository
        )

        viewModel.event.test {

            viewModel.handleIntent(UnknownIntent.PlayMedia(media = MediaMockups.unknownEpisode))
            val event = awaitItem()

            event shouldBe UnknownEvent.LaunchExternalPlayer(MediaMockups.unknownEpisode)

        }
    }

    test("back button") {
        viewModel.event.test {

            viewModel.handleIntent(UnknownIntent.OnBackTap)
            val event = awaitItem()

            event shouldBe UnknownEvent.BackToPreviousScreen

        }
    }

})