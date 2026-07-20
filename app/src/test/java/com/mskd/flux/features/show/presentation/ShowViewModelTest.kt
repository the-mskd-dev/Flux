package com.mskd.flux.features.show.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class ShowViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: ShowViewModel
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var resetProgress: ResetProgressUseCase

    val updateVm: () -> Unit = {

        resetProgress = mockk(relaxed = true)

        viewModel = ShowViewModel(
            artworkId = MediaMockups.showArtwork.id,
            observeArtworkUseCase = observeArtworkUseCase,
            resetProgress = resetProgress
        )

    }

    beforeTest {

        observeArtworkUseCase = FakeObserveArtworkUseCase()

        updateVm()

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.state.shouldBeInstanceOf<State.Content<ShowContent>>()
            val content = (initialState.state as State.Content<ShowContent>).content
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

            coVerify { resetProgress(artwork = MediaMockups.showArtwork, season = null) }
        }
    }

    test("error state") {

        viewModel = ShowViewModel(
            artworkId = -999L,
            observeArtworkUseCase = observeArtworkUseCase,
            resetProgress = resetProgress
        )

        viewModel.uiState.test {
            val state = awaitItem()
            state.state shouldBe State.Error()
        }
    }

    test("error state when content is movie instead of show") {

        viewModel = ShowViewModel(
            artworkId = MediaMockups.movieArtwork.id,
            observeArtworkUseCase = observeArtworkUseCase,
            resetProgress = resetProgress
        )

        viewModel.uiState.test {
            val state = awaitItem()
            state.state shouldBe State.Error()
        }
    }

})