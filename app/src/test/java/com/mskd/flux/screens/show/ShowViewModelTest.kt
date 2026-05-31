package com.mskd.flux.screens.show

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.mockups.FakeArtworkUC
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.mockkProgressUC
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.useCases.progress.ProgressUC
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ShowViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: ShowViewModel
    lateinit var settingsRepository: SettingsRepository
    lateinit var artworkUC: FakeArtworkUC
    lateinit var progressUC: ProgressUC

    val updateVm: () -> Unit = {

        progressUC = mockkProgressUC()

        viewModel = ShowViewModel(
            artworkId = MediaMockups.showArtwork.id,
            artworkUC = artworkUC,
            settingsRepository = settingsRepository,
            progressUC = progressUC
        )

    }

    beforeTest {

        artworkUC = FakeArtworkUC(initialContentType = ContentType.SHOW)

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State())
        }

        updateVm()

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.state.shouldBeInstanceOf<State.Content<ShowContent>>()
            val content = initialState.state.content
            content.fullShow shouldBe MediaMockups.fullShow
            content.dialog shouldBe null

        }

    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(ShowIntent.OnBackTap)
            awaitItem() shouldBe ShowEvent.BackToPreviousScreen
        }
    }

    test("on season tap") {
        viewModel.event.test {
            viewModel.handleIntent(ShowIntent.OnSeasonTap(season = 2, rgb = 12345))
            val event = awaitItem()
            event.shouldBeInstanceOf<ShowEvent.NavigateToSeason>()
            event.artworkId shouldBe MediaMockups.showArtwork.id
            event.season shouldBe 2
            event.rgb shouldBe 12345
        }
    }

    test("show season preview dialog") {
        viewModel.uiState.test {
            awaitItem()

            val season = MediaMockups.season1
            viewModel.handleIntent(ShowIntent.ShowSeasonPreview(season = season))

            val content = (awaitItem().state as State.Content).content
            content.dialog shouldBe ShowDialog.SeasonPreview(season = season)
        }
    }

    test("show reset progress dialog") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(ShowIntent.ShowResetProgressDialog)

            val content = (awaitItem().state as State.Content).content
            content.dialog shouldBe ShowDialog.ResetProgress
        }
    }

    test("close dialog") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(ShowIntent.ShowResetProgressDialog)
            awaitItem() // Consume ShowDialog.ResetProgress state

            viewModel.handleIntent(ShowIntent.CloseDialog)
            val content = (awaitItem().state as State.Content).content
            content.dialog shouldBe null
        }
    }

    test("open show info") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.event.test {
                viewModel.handleIntent(ShowIntent.OpenShowInfo)
                val event = awaitItem()
                event.shouldBeInstanceOf<ShowEvent.OpenShowInfo>()
                event.url shouldBe MediaMockups.showArtwork.infoUrl
            }
        }
    }

    test("reset progress") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(ShowIntent.ShowResetProgressDialog)
            awaitItem()

            viewModel.handleIntent(ShowIntent.ResetProgress)
            val content = (awaitItem().state as State.Content).content
            content.dialog shouldBe null

            coVerify { progressUC.resetProgress(artwork = MediaMockups.showArtwork, season = null) }
        }
    }

    test("error state") {
        artworkUC = FakeArtworkUC(initialContentType = ContentType.SHOW)

        viewModel = ShowViewModel(
            artworkId = -999L,
            artworkUC = artworkUC,
            settingsRepository = settingsRepository,
            progressUC = progressUC
        )

        viewModel.uiState.test {
            val state = awaitItem()
            state.state shouldBe State.Error
        }
    }

    test("error state when content is movie instead of show") {
        artworkUC = FakeArtworkUC(initialContentType = ContentType.MOVIE)

        viewModel = ShowViewModel(
            artworkId = MediaMockups.movieArtwork.id,
            artworkUC = artworkUC,
            settingsRepository = settingsRepository,
            progressUC = progressUC
        )

        viewModel.uiState.test {
            val state = awaitItem()
            state.state shouldBe State.Error
        }
    }

})
