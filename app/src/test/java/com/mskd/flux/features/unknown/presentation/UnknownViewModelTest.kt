package com.mskd.flux.features.unknown.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.element
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class UnknownViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var resolvePlaybackAction: ResolvePlaybackActionUseCase
    lateinit var recordPlaybackResult: SaveProgressUseCase

    val updateVm: () -> Unit = {

        resolvePlaybackAction = mockk(relaxed = true)
        recordPlaybackResult = mockk(relaxed = true)

        viewModel = UnknownViewModel(
            observeArtworkUseCase = observeArtworkUseCase,
            settingsDataStore = settingsDataStore,
            resolvePlaybackAction = resolvePlaybackAction,
            recordPlaybackResult = recordPlaybackResult
        )

    }

    beforeTest {

        observeArtworkUseCase = FakeObserveArtworkUseCase()

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        updateVm()

    }

    context("Init") {

        test("initial state") {

            viewModel.uiState.test {

                val initialState = awaitItem()

                initialState.medias shouldBe MediaMockups.unknowns
                initialState.screen.shouldBeInstanceOf<State.Content<Unit>>()

            }

        }

    }

    context("PlayMedia") {

        test("should call resolvePlaybackAction and then launch player event") {

            checkAll(
                iterations = 20,
                Arb.element(MediaMockups.allMedias),
                Exhaustive.boolean(),
                Exhaustive.boolean(),
            ) { media, forceInternal, externalPlayerRequested ->

                // Given
                val externalPlayer = !forceInternal && externalPlayerRequested
                coEvery { resolvePlaybackAction(media = media, forceInternal = forceInternal) } returns PlaybackAction.OpenPlayer(media = media, externalPlayer = externalPlayer)

                viewModel.event.test {

                    // When
                    viewModel.handleIntent(intent = UnknownIntent.PlayMedia(media = media, forceInternal = forceInternal))

                    // Then
                    val event = awaitItem()
                    event.shouldBeInstanceOf<UnknownEvent.PlayMedia>()
                    event.media shouldBe media
                    event.externalPlayer shouldBe externalPlayer

                    cancelAndConsumeRemainingEvents()

                }


            }

        }

    }


    context("OnBackTap") {

        test("should emit BackToPreviousScreen event") {
            viewModel.event.test {

                viewModel.handleIntent(UnknownIntent.OnBackTap)
                val event = awaitItem()

                event shouldBe UnknownEvent.BackToPreviousScreen

            }
        }

    }

    context("OnInfoTap") {

        test("should emit NavigateToHowToScreen event") {
            viewModel.event.test {
                viewModel.handleIntent(UnknownIntent.OnInfoTap)
                val event = awaitItem()
                event shouldBe UnknownEvent.NavigateToHowToScreen
            }
        }


    }

    context("OnExternalPlayerResult") {

        test("should save progress for external player result") {
            viewModel.uiState.test {
                awaitItem()

                viewModel.handleIntent(UnknownIntent.PlayMedia(media = MediaMockups.unknownEpisode))
                viewModel.handleIntent(UnknownIntent.OnExternalPlayerResult(progress = 5000L))

                coVerify { recordPlaybackResult(media = MediaMockups.unknownEpisode, progress = 5000L) }
            }
        }

    }

    context("DoSearch") {

        test("search word with result should return filtered medias") {

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(UnknownIntent.DoSearch("unknown movie"))

                val state = awaitItem()

                state.searchQuery shouldBe "unknown movie"
                state.filteredMedias.size shouldBe 1
            }

        }

        test("search word with no result should return empty result") {

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(UnknownIntent.DoSearch("AAA"))

                val state = awaitItem()

                state.searchQuery shouldBe "AAA"
                state.filteredMedias.size shouldBe 0

            }

        }

    }

})