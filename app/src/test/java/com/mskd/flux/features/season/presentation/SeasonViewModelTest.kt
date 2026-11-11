package com.mskd.flux.features.season.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.MarkPreviousAsWatchedUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.system.FilesLauncher
import com.mskd.flux.system.UrlLauncher
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class SeasonViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: SeasonViewModel
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var tokenDataStore: TokenDataStore
    lateinit var filesLauncher: FilesLauncher
    lateinit var urlLauncher: UrlLauncher
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var changeMediaStatus: ChangeMediaStatusUseCase
    lateinit var markPreviousAsWatched: MarkPreviousAsWatchedUseCase
    lateinit var resetProgress: ResetProgressUseCase
    lateinit var resolvePlaybackAction: ResolvePlaybackActionUseCase
    lateinit var recordPlaybackResult: SaveProgressUseCase
    lateinit var token: MutableStateFlow<String>
    lateinit var syncState: MutableStateFlow<SyncState>

    val updateVm: (id: Long, season: Int) -> Unit = { id, season ->

        filesLauncher = mockk(relaxed = true)
        urlLauncher = mockk(relaxed = true)
        changeMediaStatus = mockk(relaxed = true)
        markPreviousAsWatched = mockk(relaxed = true)
        resetProgress = mockk(relaxed = true)
        resolvePlaybackAction = mockk(relaxed = true)
        recordPlaybackResult = mockk(relaxed = true)

        viewModel = SeasonViewModel(
            artworkId = id,
            season = season,
            observeArtworkUseCase = observeArtworkUseCase,
            settingsDataStore = settingsDataStore,
            tokenDataStore = tokenDataStore,
            catalogSyncCoordinator = mockk<CatalogSyncCoordinator> { every { state } returns syncState },
            changeMediaStatus = changeMediaStatus,
            markPreviousAsWatched = markPreviousAsWatched,
            resetProgress = resetProgress,
            resolvePlaybackAction = resolvePlaybackAction,
            recordPlaybackResult = recordPlaybackResult,
            filesLauncher = filesLauncher,
            urlLauncher = urlLauncher,
        )

    }

    beforeTest {

        observeArtworkUseCase = FakeObserveArtworkUseCase()

        token = MutableStateFlow("token")
        syncState = MutableStateFlow(SyncState.Idle())
        tokenDataStore = mockk(relaxed = true) {
            coEvery { tokenIsAvailable() } returns true
            every { flow } returns token
        }

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        updateVm(MediaMockups.showArtwork.id, 1)

    }

    context("loading") {

        test("lists the episodes of its season only and selects the first one to watch") {
            viewModel.uiState.test {
                val state = awaitItem().state
                state.shouldBeInstanceOf<State.Content<SeasonContent>>()
                val content = state.content
                content.fullShow.artwork shouldBe MediaMockups.showArtwork
                content.season shouldBe 1
                content.episodes shouldBe listOf(MediaMockups.episode1, MediaMockups.episode2)
                content.selectedEpisode shouldBe MediaMockups.episode1
                content.title shouldBe MediaMockups.season1.title
                content.dialog shouldBe null
            }
        }

        test("another season lists its own episodes") {
            updateVm(MediaMockups.showArtwork.id, 2)
            viewModel.uiState.test {
                val content = (awaitItem().state as State.Content).content
                content.season shouldBe 2
                content.episodes shouldBe listOf(MediaMockups.episode3)
                content.selectedEpisode shouldBe MediaMockups.episode3
            }
        }

        test("a movie cannot be shown as a season") {
            updateVm(MediaMockups.movieArtwork.id, 1)
            viewModel.uiState.test {
                awaitItem().state shouldBe State.Error()
            }
        }

        test("an unknown artwork is an error") {
            updateVm(-999L, 1)
            viewModel.uiState.test {
                awaitItem().state shouldBe State.Error()
            }
        }

    }

    /** The current content, when the last intent may not have changed it. */
    fun current(): SeasonContent = (viewModel.uiState.value.state as State.Content).content

    suspend fun ReceiveTurbine<SeasonUiState>.content(): SeasonContent =
        (expectMostRecentItem().state as State.Content).content

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(SeasonIntent.OnBackTap)
            awaitItem() shouldBe SeasonEvent.BackToPreviousScreen
        }
    }

    context("playback") {

        test("PlayMedia selects the episode and opens the player chosen by resolvePlaybackAction") {
            listOf(true, false).forEach { externalPlayer ->
                coEvery { resolvePlaybackAction(media = MediaMockups.episode2, forceInternal = false) } returns
                    PlaybackAction.OpenPlayer(media = MediaMockups.episode2, externalPlayer = externalPlayer)
                viewModel.uiState.test {
                    expectMostRecentItem()
                    viewModel.event.test {
                        viewModel.handleIntent(SeasonIntent.PlayMedia(media = MediaMockups.episode2))
                        awaitItem() shouldBe SeasonEvent.PlayMedia(media = MediaMockups.episode2, externalPlayer = externalPlayer)
                    }
                    current().selectedEpisode shouldBe MediaMockups.episode2
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        test("the internal player is forced as a fallback of the external player") {
            coEvery { resolvePlaybackAction(media = MediaMockups.episode1, forceInternal = true) } returns
                PlaybackAction.OpenPlayer(media = MediaMockups.episode1, externalPlayer = false)
            viewModel.event.test {
                viewModel.handleIntent(SeasonIntent.PlayMedia(media = MediaMockups.episode1, forceInternal = true))
                awaitItem() shouldBe SeasonEvent.PlayMedia(media = MediaMockups.episode1, externalPlayer = false)
            }
        }

        test("the external player result is recorded on the selected episode") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.PlayMedia(MediaMockups.episode2))
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.OnExternalPlayerResult(progress = 5000L))
                coVerify { recordPlaybackResult(media = MediaMockups.episode2, progress = 5000L) }
            }
        }

    }

    context("watch status") {

        test("marks the first episode as watched without asking for the previous ones") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.ChangeWatchStatus(media = MediaMockups.episode1))
                coVerify { changeMediaStatus(media = MediaMockups.episode1, status = Status.WATCHED) }
                current().dialog shouldBe null
            }
        }

        test("marking a later episode asks to mark the previous ones, which can be closed") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.ChangeWatchStatus(media = MediaMockups.episode2))
                content().dialog shouldBe SeasonDialog.EpisodeStatusConfirmation(episode = MediaMockups.episode2)

                viewModel.handleIntent(SeasonIntent.CloseDialog)
                content().dialog shouldBe null
            }
        }

        test("validating the dialog marks the previous episodes as watched") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.ChangeWatchStatus(media = MediaMockups.episode2))
                content()
                viewModel.handleIntent(SeasonIntent.MarkPreviousEpisodesAsWatched)
                coVerify { markPreviousAsWatched(episode = MediaMockups.episode2) }
                content().dialog shouldBe null
            }
        }

        test("the previous episodes of earlier seasons count too") {
            updateVm(MediaMockups.showArtwork.id, 2)
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.ChangeWatchStatus(media = MediaMockups.episode3))
                content().dialog shouldBe SeasonDialog.EpisodeStatusConfirmation(episode = MediaMockups.episode3)
            }
        }

    }

    context("reset progress") {

        test("the dialog opens and closes") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.ShowResetProgressDialog)
                content().dialog shouldBe SeasonDialog.ResetProgressConfirmation
                viewModel.handleIntent(SeasonIntent.CloseDialog)
                content().dialog shouldBe null
            }
        }

        test("resets the season only, selects its first episode and closes the dialog") {
            updateVm(MediaMockups.showArtwork.id, 1)
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.PlayMedia(MediaMockups.episode2))
                viewModel.handleIntent(SeasonIntent.ShowResetProgressDialog)
                content().selectedEpisode shouldBe MediaMockups.episode2

                viewModel.handleIntent(SeasonIntent.ResetProgress)
                coVerify { resetProgress(MediaMockups.showArtwork, 1) }
                val content = content()
                content.selectedEpisode shouldBe MediaMockups.episode1
                content.dialog shouldBe null
            }
        }

    }

    context("links") {

        test("more info opens the TMDB page of the season") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.OpenSeasonInfo)
                verify { urlLauncher.open(url = MediaMockups.season1.infoUrl) }
            }
        }

        test("episode info opens the TMDB page of the episode") {
            viewModel.handleIntent(SeasonIntent.OpenEpisodeInfo(MediaMockups.episode1))
            verify { urlLauncher.open(url = MediaMockups.episode1.infoUrl) }
        }

        test("open file explorer opens the file of the episode") {
            viewModel.handleIntent(SeasonIntent.OpenFileExplorer(MediaMockups.episode1))
            verify { filesLauncher.open(file = MediaMockups.episode1.file) }
        }

    }

    context("gallery") {

        test("the artwork image opens the gallery of the Show when a token is available") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.event.test {
                    viewModel.handleIntent(SeasonIntent.OnArtworkImageClick)
                    awaitItem() shouldBe SeasonEvent.NavigateToGallery(artworkId = MediaMockups.showArtwork.id, contentType = ContentType.SHOW)
                }
            }
        }

        test("does nothing without token") {
            coEvery { tokenDataStore.tokenIsAvailable() } returns false
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.event.test {
                    viewModel.handleIntent(SeasonIntent.OnArtworkImageClick)
                    expectNoEvents()
                }
            }
        }

    }

    context("episode description") {

        test("expands then collapses") {
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(SeasonIntent.ExpandEpisodeDescription(episode = MediaMockups.episode1))
                content().expandedEpisodeId shouldBe MediaMockups.episode1.id
                viewModel.handleIntent(SeasonIntent.CollapseEpisodeDescription)
                content().expandedEpisodeId shouldBe null
            }
        }

    }

    context("spoiler masking") {

        val settingsFlow = MutableStateFlow(SettingsDataStore.State(blurSpoilers = true))
        val ep1 = MediaMockups.episode1
        val seasonIds = setOf(MediaMockups.episode1.id, MediaMockups.episode2.id)

        fun open(episodes: List<Episode> = MediaMockups.episodes, blurSpoilers: Boolean = true) {
            settingsFlow.value = SettingsDataStore.State(blurSpoilers = blurSpoilers)
            every { settingsDataStore.flow } returns settingsFlow
            observeArtworkUseCase = mockk {
                every { this@mockk(any()) } returns flowOf(State.Content(MediaMockups.fullShow.copy(episodes = episodes.toImmutableList())))
            }
            updateVm(MediaMockups.showArtwork.id, 1)
        }

        suspend fun blurredIds(): Set<Long> =
            (viewModel.uiState.first { it.state is State.Content }.state as State.Content).content.blurredEpisodeIds

        test("nothing is masked when the option is disabled") {
            open(blurSpoilers = false)
            blurredIds() shouldBe emptySet()
        }

        test("the episodes to watch of the season are masked, not those of other seasons") {
            open()
            blurredIds() shouldBe seasonIds
        }

        test("watching and watched episodes are readable") {
            open(episodes = listOf(MediaMockups.episode1, MediaMockups.episode2.copy(status = Status.IS_WATCHING)))
            blurredIds() shouldBe setOf(ep1.id)
        }

        test("unknown artwork episodes are never masked") {
            open(episodes = listOf(MediaMockups.unknownEpisode.copy(season = 1)))
            blurredIds() shouldBe emptySet()
        }

        test("toggling the option while the page is open updates the state") {
            open(blurSpoilers = false)
            viewModel.uiState.test {
                content().blurredEpisodeIds shouldBe emptySet()
                settingsFlow.value = SettingsDataStore.State(blurSpoilers = true)
                content().blurredEpisodeIds shouldBe seasonIds
            }
        }

        test("toggling reveals then hides an episode, the spoiler ids staying listed") {
            open()
            viewModel.uiState.test {
                content().blurredEpisodeIds shouldBe seasonIds
                viewModel.handleIntent(SeasonIntent.ToggleEpisodeSpoiler(ep1))
                val revealed = content()
                revealed.blurredEpisodeIds shouldBe seasonIds - ep1.id
                revealed.spoilerEpisodeIds shouldBe seasonIds
                viewModel.handleIntent(SeasonIntent.ToggleEpisodeSpoiler(ep1))
                content().blurredEpisodeIds shouldBe seasonIds
            }
        }

        test("an expanded episode is readable, and masked again once collapsed") {
            open()
            viewModel.uiState.test {
                content()
                viewModel.handleIntent(SeasonIntent.ExpandEpisodeDescription(ep1))
                content().blurredEpisodeIds shouldBe seasonIds - ep1.id
                viewModel.handleIntent(SeasonIntent.CollapseEpisodeDescription)
                content().blurredEpisodeIds shouldBe seasonIds
            }
        }

        test("the artwork image of the selected episode to watch is masked with it") {
            open()
            viewModel.uiState.test {
                content().isArtworkImageBlurred shouldBe true
                viewModel.handleIntent(SeasonIntent.ToggleEpisodeSpoiler(ep1))
                content().isArtworkImageBlurred shouldBe false
            }
        }

        test("tapping the artwork image of a masked episode reveals then hides it, without opening the gallery") {
            open()
            viewModel.uiState.test {
                content().isArtworkImageBlurred shouldBe true
                viewModel.event.test {
                    viewModel.handleIntent(SeasonIntent.OnArtworkImageClick)
                    expectNoEvents()
                }
                content().isArtworkImageBlurred shouldBe false
                viewModel.handleIntent(SeasonIntent.OnArtworkImageClick)
                content().isArtworkImageBlurred shouldBe true
            }
        }

    }

    context("overview") {

        val settingsFlow = MutableStateFlow(SettingsDataStore.State(blurSpoilers = true))

        fun open(
            episodes: List<Episode> = MediaMockups.episodes,
            seasons: List<Season> = MediaMockups.seasons,
            description: String = "Show description",
            blurSpoilers: Boolean = true,
        ) {
            settingsFlow.value = SettingsDataStore.State(blurSpoilers = blurSpoilers)
            every { settingsDataStore.flow } returns settingsFlow
            val show = MediaMockups.fullShow.copy(
                artwork = MediaMockups.showArtwork.copy(description = description),
                seasons = seasons.toImmutableList(),
                episodes = episodes.toImmutableList(),
            )
            observeArtworkUseCase = mockk { every { this@mockk(any()) } returns flowOf(State.Content<FullArtwork>(show)) }
            updateVm(MediaMockups.showArtwork.id, 1)
        }

        suspend fun loaded(): SeasonContent =
            (viewModel.uiState.first { it.state is State.Content }.state as State.Content).content

        val started = MediaMockups.episodes.map { if (it.id == MediaMockups.episode1.id) it.copy(status = Status.WATCHED) else it }

        test("a season entirely to watch shows its description, masked as a spoiler") {
            open()
            val content = loaded()
            content.overview shouldBe ArtworkOverviewContent.Summary(MediaMockups.season1.description)
            content.isOverviewSpoiler shouldBe true
            content.isOverviewBlurred shouldBe true
        }

        test("toggling reveals then hides the season description") {
            open()
            viewModel.uiState.test {
                content().isOverviewBlurred shouldBe true
                viewModel.handleIntent(SeasonIntent.ToggleOverviewSpoiler)
                content().isOverviewBlurred shouldBe false
                viewModel.handleIntent(SeasonIntent.ToggleOverviewSpoiler)
                content().isOverviewBlurred shouldBe true
            }
        }

        test("a started season shows the next episode to watch, masked with its artwork image until revealed") {
            open(episodes = started)
            viewModel.uiState.test {
                var content = content()
                content.overview shouldBe ArtworkOverviewContent.EpisodeOverview(started[1])
                content.isOverviewBlurred shouldBe true
                content.isArtworkImageBlurred shouldBe true

                viewModel.handleIntent(SeasonIntent.ToggleOverviewSpoiler)
                content = content()
                content.isOverviewBlurred shouldBe false
                content.isArtworkImageBlurred shouldBe false
            }
        }

        test("an episode in progress is shown and readable") {
            val episodes = MediaMockups.episodes.map { if (it.id == MediaMockups.episode1.id) it.copy(status = Status.IS_WATCHING) else it }
            open(episodes = episodes)
            val content = loaded()
            content.overview shouldBe ArtworkOverviewContent.EpisodeOverview(episodes[0])
            content.isOverviewBlurred shouldBe false
        }

        test("a season entirely watched shows its description, readable") {
            open(episodes = MediaMockups.episodes.map { it.copy(status = Status.WATCHED) })
            val content = loaded()
            content.overview shouldBe ArtworkOverviewContent.Summary(MediaMockups.season1.description)
            content.isOverviewBlurred shouldBe false
        }

        test("a season without description falls back to the readable show description") {
            open(seasons = listOf(MediaMockups.season1.copy(description = "")))
            val content = loaded()
            content.overview shouldBe ArtworkOverviewContent.Summary("Show description")
            content.isOverviewBlurred shouldBe false
        }

        test("no description at all yields no summary") {
            open(seasons = listOf(MediaMockups.season1.copy(description = "")), description = "")
            loaded().overview shouldBe ArtworkOverviewContent.Summary(null)
        }

        test("the season description is readable when the option is disabled") {
            open(blurSpoilers = false)
            loaded().isOverviewBlurred shouldBe false
        }

    }

    context("Manual identification") {

        suspend fun identification(): IdentificationAvailability =
            (viewModel.uiState.first { it.state is State.Content }.state as State.Content).content.identification

        test("offered on a Season with a TMDB token") {
            identification() shouldBe IdentificationAvailability.ENABLED
        }

        test("hidden without TMDB token") {
            token.value = ""
            updateVm(MediaMockups.showArtwork.id, 1)
            identification() shouldBe IdentificationAvailability.HIDDEN
        }

        test("disabled during a Sync, and the dialog opens only once it ends") {
            syncState.value = SyncState.Syncing(full = false)
            updateVm(MediaMockups.showArtwork.id, 1)
            viewModel.uiState.test {
                content().identification shouldBe IdentificationAvailability.DISABLED
                viewModel.handleIntent(SeasonIntent.ShowIdentificationDialog)
                expectNoEvents()

                syncState.value = SyncState.Idle()
                content().identification shouldBe IdentificationAvailability.ENABLED
                viewModel.handleIntent(SeasonIntent.ShowIdentificationDialog)
                content().dialog shouldBe SeasonDialog.Identification
            }
        }

        listOf(ContentType.SHOW, ContentType.MOVIE).forEach { type ->
            test("once identified as a $type, the dialog closes and the new Artwork is sent with its type") {
                viewModel.uiState.test {
                    expectMostRecentItem()
                    viewModel.handleIntent(SeasonIntent.ShowIdentificationDialog)
                    content().dialog shouldBe SeasonDialog.Identification
                    viewModel.event.test {
                        viewModel.handleIntent(SeasonIntent.OnIdentified(artworkId = 42L, type = type))
                        awaitItem() shouldBe SeasonEvent.Identified(artworkId = 42L, type = type)
                    }
                    content().dialog shouldBe null
                }
            }
        }

    }

})
