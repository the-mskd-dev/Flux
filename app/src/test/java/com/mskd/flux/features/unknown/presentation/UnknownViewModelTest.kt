package com.mskd.flux.features.unknown.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class UnknownViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var saveProgress: SaveProgressUseCase

    val updateVm: () -> Unit = {

        saveProgress = mockk(relaxed = true)

        viewModel = UnknownViewModel(
            observeArtworkUseCase = observeArtworkUseCase,
            settingsDataStore = settingsDataStore,
            saveProgress = saveProgress
        )

    }

    beforeTest {

        observeArtworkUseCase = FakeObserveArtworkUseCase()

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        updateVm()

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.medias shouldBe MediaMockups.unknowns
            initialState.screen.shouldBeInstanceOf<State.Content<Unit>>()

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

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State(externalPlayer = true))
        }

        updateVm()

        viewModel.uiState.test {

            awaitItem()

            viewModel.event.test {

                viewModel.handleIntent(UnknownIntent.PlayMedia(media = MediaMockups.unknownEpisode))
                val event = awaitItem()

                event shouldBe UnknownEvent.LaunchExternalPlayer(MediaMockups.unknownEpisode)

            }

        }
    }

    test("back button") {
        viewModel.event.test {

            viewModel.handleIntent(UnknownIntent.OnBackTap)
            val event = awaitItem()

            event shouldBe UnknownEvent.BackToPreviousScreen

        }
    }

    test("on info tap") {
        viewModel.event.test {
            viewModel.handleIntent(UnknownIntent.OnInfoTap)
            val event = awaitItem()
            event shouldBe UnknownEvent.NavigateToHowToScreen
        }
    }

    test("play media - force internal player when external enabled") {
        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State(externalPlayer = true))
        }

        updateVm()

        viewModel.uiState.test {
            awaitItem()

            viewModel.event.test {
                viewModel.handleIntent(UnknownIntent.PlayMedia(media = MediaMockups.unknownEpisode, forceInternal = true))
                val event = awaitItem()

                event shouldBe UnknownEvent.PlayMedia(MediaMockups.unknownEpisode.id)
            }
        }
    }

    test("on external player result") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(UnknownIntent.PlayMedia(media = MediaMockups.unknownEpisode))
            viewModel.handleIntent(UnknownIntent.OnExternalPlayerResult(progress = 5000L))

            coVerify { saveProgress(media = MediaMockups.unknownEpisode, progress = 5000L) }
        }
    }

    test("search word with result") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(UnknownIntent.DoSearch("unknown movie"))

            val state = awaitItem()

            state.searchQuery shouldBe "unknown movie"
            state.filteredMedias.size shouldBe 1
        }

    }

    test("search word with no result") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(UnknownIntent.DoSearch("AAA"))

            val state = awaitItem()

            state.searchQuery shouldBe "AAA"
            state.filteredMedias.size shouldBe 0

        }

    }

})