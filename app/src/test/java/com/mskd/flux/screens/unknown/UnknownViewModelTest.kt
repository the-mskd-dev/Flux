package com.mskd.flux.screens.unknown

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.mockups.FakeArtworkUC
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.mockkDatabaseRepository
import com.mskd.flux.model.ScreenState
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.useCases.progress.ProgressUCImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class UnknownViewModelTest : FunSpec ({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var artworkRepository: FakeArtworkUC
    lateinit var settingsRepository: SettingsRepository
    lateinit var userRepository: UserRepository
    lateinit var progressUC: ProgressUC
    lateinit var databaseRepository: DatabaseRepository

    val updateVm: () -> Unit = {

        progressUC = ProgressUCImpl(
            database = databaseRepository,
            user = userRepository,
        )

        viewModel = UnknownViewModel(
            artworkUC = artworkRepository,
            settingsRepository = settingsRepository,
            progressUC = progressUC
        )

    }

    beforeTest {

        artworkRepository = FakeArtworkUC()

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State())
        }

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserRepository.State())
        }

        databaseRepository = mockkDatabaseRepository()

        updateVm()

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
        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State(externalPlayer = true))
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

            coVerify { databaseRepository.saveEpisodes(match { episodes ->
                episodes.any { it.id == MediaMockups.unknownEpisode.id && it.currentTime == 5000L }
            }) }
        }
    }

})