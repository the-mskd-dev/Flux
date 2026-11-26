package com.mskd.flux.features.unknown.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.features.player.domain.playback.PlaybackLaunch
import com.mskd.flux.features.player.domain.playback.PlaybackOutcome
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.system.FilesLauncher
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class UnknownViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: UnknownViewModel
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var tokenDataStore: TokenDataStore
    lateinit var playbackLaunch: PlaybackLaunch
    lateinit var filesLauncher: FilesLauncher
    lateinit var token: MutableStateFlow<String>
    lateinit var syncState: MutableStateFlow<SyncState>

    val updateVm: () -> Unit = {

        playbackLaunch = mockk(relaxed = true)
        filesLauncher = mockk(relaxed = true)

        viewModel = UnknownViewModel(
            observeArtworkUseCase = observeArtworkUseCase,
            settingsDataStore = settingsDataStore,
            tokenDataStore = tokenDataStore,
            catalogSyncCoordinator = mockk<CatalogSyncCoordinator> { every { state } returns syncState },
            playbackLaunch = playbackLaunch,
            filesLauncher = filesLauncher
        )

    }

    beforeTest {

        observeArtworkUseCase = FakeObserveArtworkUseCase()

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        token = MutableStateFlow("token")
        syncState = MutableStateFlow(SyncState.Idle())
        tokenDataStore = mockk(relaxed = true) {
            every { flow } returns token
        }

        updateVm()

    }

    context("Init") {

        test("initial state") {

            viewModel.uiState.test {

                val initialState = awaitItem()

                initialState.medias shouldBe MediaMockups.unknowns
                initialState.screen.shouldBeInstanceOf<State.Content<Unit>>()
                initialState.dialog shouldBe null

            }

        }

    }

    context("PlayMedia") {

        val media = MediaMockups.unknownEpisode

        test("the Internal player outcome opens the Internal player") {
            coEvery { playbackLaunch.play(media = media, forceInternal = true) } returns PlaybackOutcome.OpenInternalPlayer(media)
            viewModel.event.test {
                viewModel.handleIntent(UnknownIntent.PlayMedia(media = media, forceInternal = true))
                awaitItem() shouldBe UnknownEvent.OpenInternalPlayer(media = media)
            }
        }

        test("a Media handed to the External player or Unavailable emits nothing") {
            listOf(PlaybackOutcome.HandedToExternalPlayer, PlaybackOutcome.Unavailable).forEach { outcome ->
                coEvery { playbackLaunch.play(media = media, forceInternal = false) } returns outcome
                viewModel.event.test {
                    viewModel.handleIntent(UnknownIntent.PlayMedia(media = media))
                    expectNoEvents()
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

    context("OpenFileExplorer") {

        test("should call filesLauncher") {

            // Given
            val media = MediaMockups.unknownEpisode

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(UnknownIntent.OpenFileExplorer(media))

                // Then
                verify { filesLauncher.open(file = media.file) }

            }


        }
    }

    context("Manual identification") {

        val media = MediaMockups.unknownEpisode

        suspend fun identification(): IdentificationAvailability =
            viewModel.uiState.first { it.screen is State.Content }.identification

        test("offered with a TMDB token") {
            identification() shouldBe IdentificationAvailability.ENABLED
        }

        test("hidden without TMDB token") {
            token.value = ""
            updateVm()
            identification() shouldBe IdentificationAvailability.HIDDEN
        }

        test("disabled during a Sync, enabled again once it ends") {
            syncState.value = SyncState.Syncing(type = SyncType.LIGHT)
            updateVm()
            viewModel.uiState.test {
                awaitItem().identification shouldBe IdentificationAvailability.DISABLED
                syncState.value = SyncState.Idle()
                awaitItem().identification shouldBe IdentificationAvailability.ENABLED
            }
        }

        test("ShowIdentificationDialog is ignored when hidden without token") {
            token.value = ""
            updateVm()
            viewModel.uiState.test {
                awaitItem().identification shouldBe IdentificationAvailability.HIDDEN
                viewModel.handleIntent(UnknownIntent.ShowIdentificationDialog(media = media))
                expectNoEvents()
            }
        }

        test("the dialog opens only when the option is enabled") {
            syncState.value = SyncState.Syncing(type = SyncType.LIGHT)
            updateVm()
            viewModel.uiState.test {
                awaitItem().identification shouldBe IdentificationAvailability.DISABLED
                viewModel.handleIntent(UnknownIntent.ShowIdentificationDialog(media = media))
                expectNoEvents()

                syncState.value = SyncState.Idle()
                awaitItem().identification shouldBe IdentificationAvailability.ENABLED
                viewModel.handleIntent(UnknownIntent.ShowIdentificationDialog(media = media))
                awaitItem().dialog shouldBe UnknownDialog.Identification(media = media)
            }
        }

        test("CloseDialog closes the identification dialog") {
            viewModel.uiState.test {
                awaitItem()
                viewModel.handleIntent(UnknownIntent.ShowIdentificationDialog(media = media))
                awaitItem().dialog shouldBe UnknownDialog.Identification(media = media)

                viewModel.handleIntent(UnknownIntent.CloseDialog)
                awaitItem().dialog shouldBe null
            }
        }

        test("OnIdentified closes the identification dialog without emitting navigation events") {
            viewModel.uiState.test {
                awaitItem()
                viewModel.handleIntent(UnknownIntent.ShowIdentificationDialog(media = media))
                awaitItem().dialog shouldBe UnknownDialog.Identification(media = media)

                viewModel.event.test {
                    viewModel.handleIntent(UnknownIntent.OnIdentified)
                    expectNoEvents()
                }
                awaitItem().dialog shouldBe null
            }
        }

    }

})