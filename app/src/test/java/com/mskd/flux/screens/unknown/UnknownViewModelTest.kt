package com.mskd.flux.screens.unknown

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.data.datastore.SettingsDataStore
import com.mskd.flux.core.data.datastore.UserDataStore
import com.mskd.flux.core.domain.model.core.State
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.mockups.FakeArtworkUC
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.core.FakeDatabaseRepository
import com.mskd.flux.screen.unknown.UnknownEvent
import com.mskd.flux.screen.unknown.UnknownIntent
import com.mskd.flux.screen.unknown.UnknownViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class UnknownViewModelTest : FunSpec ({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var artworkRepository: FakeArtworkUC
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var userDataStore: UserDataStore
    lateinit var saveProgress: SaveProgressUseCase
    lateinit var databaseRepository: DatabaseRepository

    val updateVm: () -> Unit = {

        saveProgress = mockk(relaxed = true)

        viewModel = UnknownViewModel(
            artworkUC = artworkRepository,
            settingsDataStore = settingsDataStore,
            saveProgress = saveProgress
        )

    }

    beforeTest {

        artworkRepository = FakeArtworkUC()

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        userDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserDataStore.State())
        }

        databaseRepository = FakeDatabaseRepository()

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