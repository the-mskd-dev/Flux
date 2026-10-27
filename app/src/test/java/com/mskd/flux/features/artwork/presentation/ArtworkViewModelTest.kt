package com.mskd.flux.features.artwork.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.people.domain.usecase.GetPeopleUseCase
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.MarkPreviousAsWatchedUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.system.FilesLauncher
import com.mskd.flux.system.UrlLauncher
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: ArtworkViewModel
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var tokenDataStore: TokenDataStore
    lateinit var filesLauncher: FilesLauncher
    lateinit var urlLauncher: UrlLauncher
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var getPeopleUseCase: GetPeopleUseCase
    lateinit var changeMediaStatus: ChangeMediaStatusUseCase
    lateinit var markPreviousAsWatched: MarkPreviousAsWatchedUseCase
    lateinit var resetProgress: ResetProgressUseCase
    lateinit var resolvePlaybackAction: ResolvePlaybackActionUseCase
    lateinit var recordPlaybackResult: SaveProgressUseCase

    var currentSeason: Int? = 1

    val updateVm: (id: Long) -> Unit = { id ->

        filesLauncher = mockk(relaxed = true)
        urlLauncher = mockk(relaxed = true)
        changeMediaStatus = mockk(relaxed = true)
        markPreviousAsWatched = mockk(relaxed = true)
        resetProgress = mockk(relaxed = true)
        resolvePlaybackAction = mockk(relaxed = true)
        recordPlaybackResult = mockk(relaxed = true)


        viewModel = ArtworkViewModel(
            artworkId = id,
            season = currentSeason,
            observeArtworkUseCase = observeArtworkUseCase,
            getPeopleUseCase = getPeopleUseCase,
            settingsDataStore = settingsDataStore,
            tokenDataStore = tokenDataStore,
            changeMediaStatus = changeMediaStatus,
            markPreviousAsWatched = markPreviousAsWatched,
            resetProgress = resetProgress,
            resolvePlaybackAction = resolvePlaybackAction,
            recordPlaybackResult = recordPlaybackResult,
            filesLauncher = filesLauncher,
            urlLauncher = urlLauncher
        )

    }

    beforeTest {

        currentSeason = 1

        observeArtworkUseCase = FakeObserveArtworkUseCase()
        getPeopleUseCase = mockk()
        every { getPeopleUseCase(any()) } returns MutableStateFlow(emptyList<Person>().toImmutableList())

        tokenDataStore = mockk(relaxed = true) { coEvery { tokenIsAvailable() } returns true }

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        }

        updateVm(MediaMockups.showArtwork.id)

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.state.shouldBeInstanceOf<State.Content<ArtworkContent>>()
            val content = (initialState.state as State.Content<ArtworkContent>).content
            content.fullArtwork.artwork shouldBe MediaMockups.showArtwork
            content.selectedMedia shouldBe MediaMockups.episode1
            (content.fullArtwork as FullArtwork.FullShow).episodes shouldBe MediaMockups.episodes
            content.selectedSeason shouldBe MediaMockups.episode1.season
            content.dialog shouldBe null

        }

    }

    test("exposes person in content") {

        // Given
        val person = listOf(
            Person(
                id = 819,
                creditId = "credit-819",
                artworkId = MediaMockups.showArtwork.id,
                name = "Edward Norton",
                profilePath = "/profile.jpg",
                role = "The Narrator",
                type = PersonType.CAST,
                personOrder = 0
            )
        )
        every { getPeopleUseCase(any()) } returns MutableStateFlow(person.toImmutableList())
        updateVm(MediaMockups.showArtwork.id)

        // When / Then
        viewModel.uiState.test {
            val content = (awaitItem().state as State.Content<ArtworkContent>).content
            content.people shouldBe person.toImmutableList()
        }

    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(ArtworkIntent.OnBackTap)
            awaitItem() shouldBe ArtworkEvent.BackToPreviousScreen
        }
    }

    test("instantiate with specific season") {

        currentSeason = 2
        updateVm(MediaMockups.showArtwork.id)

        viewModel.uiState.test {

            val state = awaitItem()
            val content = (state.state as State.Content).content
            content.selectedSeason shouldBe 2

        }

    }

    test("PlayMedia - should call resolvePlaybackAction and then launch player event") {

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
                viewModel.handleIntent(intent = ArtworkIntent.PlayMedia(media = media, forceInternal = forceInternal))

                // Then
                val event = awaitItem()
                event.shouldBeInstanceOf<ArtworkEvent.PlayMedia>()
                event.media shouldBe media
                event.externalPlayer shouldBe externalPlayer

                cancelAndConsumeRemainingEvents()

            }


        }

    }

    test("mark first episode as watched") {

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val content = (initialState.state as State.Content).content
            val media = content.selectedMedia as Episode

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = media))

            coVerify {
                changeMediaStatus(
                    media = match { it.mediaId == media.mediaId },
                    status = match { it == Status.WATCHED }
                )
            }

            cancelAndConsumeRemainingEvents()

        }

    }

    test("request change watch status for second episode with previous") {

        viewModel.uiState.test {

            // Initial state
            expectMostRecentItem()

            // Request change status of episode 2
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))

            // Final state
            val updatedState = expectMostRecentItem()
            val content = (updatedState.state as State.Content).content

            content.dialog shouldBe ArtworkDialog.EpisodeStatusConfirmation(episode = MediaMockups.episode2)

            cancelAndConsumeRemainingEvents()

        }

    }

    test("close episodes status dialog") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))
            var content = (expectMostRecentItem().state as State.Content).content
            content.dialog shouldBe ArtworkDialog.EpisodeStatusConfirmation(episode = MediaMockups.episode2)

            viewModel.handleIntent(ArtworkIntent.CloseDialog)
            content = (expectMostRecentItem().state as State.Content).content
            content.dialog shouldBe null
        }
    }

    test("request change watch status for second episode without previous") {

        viewModel.uiState.test {

            // Initial state
            expectMostRecentItem()

            // Change status of episode 1
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode1))

            coVerify {
                changeMediaStatus(
                    media = match { it.mediaId == MediaMockups.episode1.mediaId },
                    status = match { it == Status.WATCHED }
                )
            }

            // Change status of episode 2
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))

            coVerify {
                changeMediaStatus(
                    media = match { it.mediaId == MediaMockups.episode2.mediaId },
                    status = match { it == Status.WATCHED }
                )
            }

            cancelAndConsumeRemainingEvents()

        }

    }

    test("mark latest episode and previous as watched") {

        viewModel.uiState.test {

            val loadedState = awaitItem()
            val content = (loadedState.state as State.Content).content
            val episodes = (content.fullArtwork as FullArtwork.FullShow).episodes

            // Change status of the latest episode
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = episodes.last()))

            val stateWithDialog = expectMostRecentItem()
            val contentWithDialog = (stateWithDialog.state as State.Content).content
            contentWithDialog.dialog shouldNotBe null

            // Validate change for previous episodes
            viewModel.handleIntent(ArtworkIntent.MarkPreviousEpisodesAsWatched)


            coVerify { markPreviousAsWatched(episode = episodes.last()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    test("mark movie as watched") {

        updateVm(MediaMockups.movieArtwork.id)

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val content = (initialState.state as State.Content).content
            val media = content.selectedMedia

            media.status shouldBe Status.TO_WATCH

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = media))

            coVerify {
                changeMediaStatus(
                    media = match { it.mediaId == media.mediaId },
                    status = match { it == Status.WATCHED }
                )
            }

            cancelAndConsumeRemainingEvents()

        }

    }

    test("show reset progress dialog") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(ArtworkIntent.ShowResetProgressDialog)
            var content = (awaitItem().state as State.Content).content
            content.dialog shouldBe ArtworkDialog.ResetProgressConfirmation

            viewModel.handleIntent(ArtworkIntent.CloseDialog)
            content = (awaitItem().state as State.Content).content
            content.dialog shouldBe null

        }
    }

    test("reset progress") {
        viewModel.uiState.test {
            val state = awaitItem()
            val content = (state.state as State.Content).content

            viewModel.handleIntent(ArtworkIntent.ResetProgress)

            coVerify { resetProgress(content.fullArtwork.artwork, 1) }

        }
    }

    context("OpenArtworkInfo") {
        test("should call UrlLauncher") {

            viewModel.uiState.test {

                // Given
                expectMostRecentItem()

                // When
                viewModel.handleIntent(ArtworkIntent.OpenArtworkInfo)

                // Then
                verify { urlLauncher.open(url = MediaMockups.season1.infoUrl) }
            }
        }
    }

    context("OpenEpisodeInfo") {
        test("should call UrlLauncher") {

            viewModel.uiState.test {

                // Given
                expectMostRecentItem()
                val episode = MediaMockups.episode1

                // When
                viewModel.handleIntent(ArtworkIntent.OpenEpisodeInfo(episode))

                // Then
                verify { urlLauncher.open(url = episode.infoUrl) }
            }
        }
    }

    test("on external player result") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.handleIntent(ArtworkIntent.PlayMedia(MediaMockups.episode1))
            viewModel.handleIntent(ArtworkIntent.OnExternalPlayerResult(progress = 5000L))
            coVerify { recordPlaybackResult(media = MediaMockups.episode1, progress = 5000L) }
        }
    }

    test("expand episode description") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.handleIntent(ArtworkIntent.ExpandEpisodeDescription(episode = MediaMockups.episode1))
            val state = expectMostRecentItem()
            val content = (state.state as State.Content).content
            content.expandedEpisodeId shouldBe MediaMockups.episode1.id
        }
    }

    test("collapse episode description") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.handleIntent(ArtworkIntent.ExpandEpisodeDescription(episode = MediaMockups.episode1))
            expectMostRecentItem()

            viewModel.handleIntent(ArtworkIntent.CollapseEpisodeDescription)
            val state = expectMostRecentItem()
            val content = (state.state as State.Content).content
            content.expandedEpisodeId shouldBe null
        }
    }

    context("OnArtworkImageClick") {

        test("navigates to the gallery when a token is available") {
            viewModel.uiState.test {
                awaitItem()
                viewModel.event.test {
                    viewModel.handleIntent(ArtworkIntent.OnArtworkImageClick)
                    awaitItem() shouldBe ArtworkEvent.NavigateToGallery(artworkId = MediaMockups.showArtwork.id, contentType = ContentType.SHOW)
                }
            }
        }

        test("does nothing without token") {
            coEvery { tokenDataStore.tokenIsAvailable() } returns false
            viewModel.event.test {
                viewModel.handleIntent(ArtworkIntent.OnArtworkImageClick)
                expectNoEvents()
            }
        }

    }

    context("OnPersonClick") {

        test("should call NavigateToPerson event") {

            // Given
            val person = PeopleMockup.cast.first()

            viewModel.event.test {

                // When
                viewModel.handleIntent(ArtworkIntent.OnPersonClick(person = person))

                // Then
                awaitItem() shouldBe ArtworkEvent.NavigateToPerson(person = person)

            }
        }

    }

    context("spoiler blur") {

        val settingsFlow = MutableStateFlow(SettingsDataStore.State())

        suspend fun blurredIds(
            blurSpoilers: Boolean = true,
            episodes: List<Episode> = MediaMockups.episodes,
        ): Set<Long> {
            settingsFlow.value = SettingsDataStore.State(blurSpoilers = blurSpoilers)
            every { settingsDataStore.flow } returns settingsFlow
            every { observeArtworkUseCase(any()) } returns flowOf(
                State.Content(MediaMockups.fullShow.copy(episodes = episodes.toImmutableList()))
            )
            updateVm(MediaMockups.showArtwork.id)
            var ids: Set<Long>? = null
            viewModel.uiState.test {
                var state = awaitItem().state
                while (state !is State.Content) state = awaitItem().state
                ids = state.content.blurredEpisodeIds
            }
            return ids!!
        }

        test("nothing is blurred when the option is disabled") {
            observeArtworkUseCase = mockk()
            blurredIds(blurSpoilers = false) shouldBe emptySet()
        }

        test("episodes to watch are blurred") {
            observeArtworkUseCase = mockk()
            blurredIds() shouldBe MediaMockups.episodes.map { it.id }.toSet()
        }

        test("watching and watched episodes are readable") {
            observeArtworkUseCase = mockk()
            val episodes = listOf(
                MediaMockups.episode1,
                MediaMockups.episode2.copy(status = Status.IS_WATCHING),
                MediaMockups.episode3.copy(status = Status.WATCHED),
            )
            blurredIds(episodes = episodes) shouldBe setOf(MediaMockups.episode1.id)
        }

        test("unknown artwork episodes are never blurred") {
            observeArtworkUseCase = mockk()
            blurredIds(episodes = listOf(MediaMockups.unknownEpisode.copy(season = 1))) shouldBe emptySet()
        }

        test("movie is never blurred") {
            observeArtworkUseCase = mockk()
            settingsFlow.value = SettingsDataStore.State(blurSpoilers = true)
            every { settingsDataStore.flow } returns settingsFlow
            every { observeArtworkUseCase(any()) } returns flowOf(State.Content(MediaMockups.fullMovie))
            updateVm(MediaMockups.fullMovie.artwork.id)
            viewModel.uiState.test {
                var state = awaitItem().state
                while (state !is State.Content) state = awaitItem().state
                state.content.blurredEpisodeIds shouldBe emptySet()
            }
        }

        test("episode becomes readable when its status changes to watching") {
            observeArtworkUseCase = mockk()
            val artwork = MutableStateFlow<State<FullArtwork>>(State.Content(MediaMockups.fullShow))
            settingsFlow.value = SettingsDataStore.State(blurSpoilers = true)
            every { settingsDataStore.flow } returns settingsFlow
            every { observeArtworkUseCase(any()) } returns artwork
            updateVm(MediaMockups.showArtwork.id)

            viewModel.uiState.test {
                (expectMostRecentItem().state as State.Content).content.blurredEpisodeIds shouldBe
                    MediaMockups.episodes.map { it.id }.toSet()

                artwork.value = State.Content(
                    MediaMockups.fullShow.copy(
                        episodes = MediaMockups.episodes.map {
                            if (it.id == MediaMockups.episode2.id) it.copy(status = Status.IS_WATCHING) else it
                        }.toImmutableList()
                    )
                )

                (expectMostRecentItem().state as State.Content).content.blurredEpisodeIds shouldBe
                    setOf(MediaMockups.episode1.id, MediaMockups.episode3.id)
            }
        }

        test("toggling the option while the page is open updates the state") {
            observeArtworkUseCase = mockk()
            settingsFlow.value = SettingsDataStore.State(blurSpoilers = false)
            every { settingsDataStore.flow } returns settingsFlow
            every { observeArtworkUseCase(any()) } returns flowOf(State.Content(MediaMockups.fullShow))
            updateVm(MediaMockups.showArtwork.id)

            viewModel.uiState.test {
                (expectMostRecentItem().state as State.Content).content.blurredEpisodeIds shouldBe emptySet()

                settingsFlow.value = SettingsDataStore.State(blurSpoilers = true)

                (expectMostRecentItem().state as State.Content).content.blurredEpisodeIds shouldBe
                    MediaMockups.episodes.map { it.id }.toSet()
            }
        }

    }

    context("spoiler reveal") {

        val settingsFlow = MutableStateFlow(SettingsDataStore.State(blurSpoilers = true))
        val ep1 = MediaMockups.episode1
        val allIds = MediaMockups.episodes.map { it.id }.toSet()

        fun openShow() {
            observeArtworkUseCase = mockk()
            every { settingsDataStore.flow } returns settingsFlow
            every { observeArtworkUseCase(any()) } returns flowOf(State.Content(MediaMockups.fullShow))
            updateVm(MediaMockups.showArtwork.id)
        }

        suspend fun ReceiveTurbine<ArtworkUiState>.blurred() =
            (expectMostRecentItem().state as State.Content).content.blurredEpisodeIds

        test("toggling reveals then hides an episode") {
            openShow()
            viewModel.uiState.test {
                blurred() shouldBe allIds

                viewModel.handleIntent(ArtworkIntent.ToggleEpisodeSpoiler(ep1))
                blurred() shouldBe allIds - ep1.id

                viewModel.handleIntent(ArtworkIntent.ToggleEpisodeSpoiler(ep1))
                blurred() shouldBe allIds
            }
        }

        test("expanding a hidden episode makes it readable, collapsing hides it again") {
            openShow()
            viewModel.uiState.test {
                blurred() shouldBe allIds

                viewModel.handleIntent(ArtworkIntent.ExpandEpisodeDescription(ep1))
                blurred() shouldBe allIds - ep1.id

                viewModel.handleIntent(ArtworkIntent.CollapseEpisodeDescription)
                blurred() shouldBe allIds
            }
        }

        test("an episode revealed by tap stays readable after expand then collapse") {
            openShow()
            viewModel.uiState.test {
                blurred() shouldBe allIds

                viewModel.handleIntent(ArtworkIntent.ToggleEpisodeSpoiler(ep1))
                viewModel.handleIntent(ArtworkIntent.ExpandEpisodeDescription(ep1))
                viewModel.handleIntent(ArtworkIntent.CollapseEpisodeDescription)
                blurred() shouldBe allIds - ep1.id
            }
        }

        test("a new view model starts without revealed episodes") {
            openShow()
            viewModel.uiState.test {
                blurred()
                viewModel.handleIntent(ArtworkIntent.ToggleEpisodeSpoiler(ep1))
                blurred() shouldBe allIds - ep1.id
            }
            updateVm(MediaMockups.showArtwork.id)
            viewModel.uiState.test {
                blurred() shouldBe allIds
            }
        }

        test("spoiler ids stay listed when an episode is revealed") {
            openShow()
            viewModel.uiState.test {
                expectMostRecentItem()
                viewModel.handleIntent(ArtworkIntent.ToggleEpisodeSpoiler(ep1))
                (expectMostRecentItem().state as State.Content).content.spoilerEpisodeIds shouldBe allIds
            }
        }

    }

    context("overview") {

        suspend fun overviewFor(fullArtwork: FullArtwork): ArtworkOverviewContent {
            every { observeArtworkUseCase(any()) } returns flowOf(State.Content(fullArtwork))
            updateVm(fullArtwork.artwork.id)
            var overview: ArtworkOverviewContent? = null
            viewModel.uiState.test {
                var state = awaitItem().state
                while (state !is State.Content) state = awaitItem().state
                overview = state.content.overview
            }
            return overview!!
        }

        fun show(
            episodes: List<Episode> = MediaMockups.episodes,
            seasons: List<Season> = MediaMockups.seasons,
            description: String = "Show description",
        ) = MediaMockups.fullShow.copy(
            artwork = MediaMockups.showArtwork.copy(description = description),
            seasons = seasons.toImmutableList(),
            episodes = episodes.toImmutableList(),
        )

        test("movie shows the movie description with its genres") {
            observeArtworkUseCase = mockk()
            overviewFor(MediaMockups.fullMovie) shouldBe ArtworkOverviewContent.MovieOverview(
                movie = MediaMockups.fullMovie.movie,
                genres = MediaMockups.fullMovie.genres,
            )
        }

        test("show with an in-progress episode shows that episode") {
            observeArtworkUseCase = mockk()
            overviewFor(show(episodes = MediaMockups.episodesWithStatus)) shouldBe
                ArtworkOverviewContent.EpisodeOverview(MediaMockups.episodesWithStatus[1])
        }

        test("season entirely to watch shows the season description") {
            observeArtworkUseCase = mockk()
            overviewFor(show()) shouldBe ArtworkOverviewContent.Summary(MediaMockups.season1.description)
        }

        test("season partially watched without an in-progress episode shows the season description") {
            observeArtworkUseCase = mockk()
            val episodes = listOf(
                MediaMockups.episode1.copy(status = Status.WATCHED),
                MediaMockups.episode2,
                MediaMockups.episode3,
            )
            overviewFor(show(episodes = episodes)) shouldBe ArtworkOverviewContent.Summary(MediaMockups.season1.description)
        }

        test("season entirely watched shows the season description") {
            observeArtworkUseCase = mockk()
            val episodes = MediaMockups.episodes.map { it.copy(status = Status.WATCHED) }
            overviewFor(show(episodes = episodes)) shouldBe ArtworkOverviewContent.Summary(MediaMockups.season1.description)
        }

        test("season without description falls back to the show description") {
            observeArtworkUseCase = mockk()
            val seasons = listOf(MediaMockups.season1.copy(description = ""))
            overviewFor(show(seasons = seasons)) shouldBe ArtworkOverviewContent.Summary("Show description")
        }

        test("no description at all yields no summary") {
            observeArtworkUseCase = mockk()
            val seasons = listOf(MediaMockups.season1.copy(description = ""))
            overviewFor(show(seasons = seasons, description = "")) shouldBe ArtworkOverviewContent.Summary(null)
        }

    }

    context("overview spoiler") {

        val settingsFlow = MutableStateFlow(SettingsDataStore.State(blurSpoilers = true))

        fun show(
            episodes: List<Episode> = MediaMockups.episodes,
            seasons: List<Season> = MediaMockups.seasons,
        ) = MediaMockups.fullShow.copy(
            artwork = MediaMockups.showArtwork.copy(description = "Show description"),
            seasons = seasons.toImmutableList(),
            episodes = episodes.toImmutableList(),
        )

        fun open(fullArtwork: FullArtwork, blurSpoilers: Boolean = true) {
            observeArtworkUseCase = mockk()
            settingsFlow.value = SettingsDataStore.State(blurSpoilers = blurSpoilers)
            every { settingsDataStore.flow } returns settingsFlow
            every { observeArtworkUseCase(any()) } returns flowOf(State.Content(fullArtwork))
            updateVm(fullArtwork.artwork.id)
        }

        suspend fun ReceiveTurbine<ArtworkUiState>.overviewBlurred() =
            (expectMostRecentItem().state as State.Content).content.isOverviewBlurred

        suspend fun blurredOf(fullArtwork: FullArtwork, blurSpoilers: Boolean = true): Boolean {
            open(fullArtwork, blurSpoilers)
            var blurred: Boolean? = null
            viewModel.uiState.test {
                var state = awaitItem().state
                while (state !is State.Content) state = awaitItem().state
                blurred = state.content.isOverviewBlurred
            }
            return blurred!!
        }

        test("season entirely to watch is blurred") {
            blurredOf(show()) shouldBe true
        }

        test("season with a watching episode is readable") {
            blurredOf(show(episodes = MediaMockups.episodes.mapIndexed { i, e ->
                if (i == 0) e.copy(status = Status.IS_WATCHING) else e
            })) shouldBe false
        }

        test("season with a watched episode is readable") {
            blurredOf(show(episodes = MediaMockups.episodes.mapIndexed { i, e ->
                if (i == 0) e.copy(status = Status.WATCHED) else e
            })) shouldBe false
        }

        test("show description fallback is readable") {
            blurredOf(show(seasons = listOf(MediaMockups.season1.copy(description = "")))) shouldBe false
        }

        test("movie to watch is readable") {
            blurredOf(MediaMockups.fullMovie) shouldBe false
        }

        test("option disabled is readable") {
            blurredOf(show(), blurSpoilers = false) shouldBe false
        }

        test("toggling reveals then hides the overview") {
            open(show())
            viewModel.uiState.test {
                overviewBlurred() shouldBe true

                viewModel.handleIntent(ArtworkIntent.ToggleOverviewSpoiler)
                overviewBlurred() shouldBe false

                viewModel.handleIntent(ArtworkIntent.ToggleOverviewSpoiler)
                overviewBlurred() shouldBe true
            }
        }

    }

    test("error state") {

        updateVm(-999L)

        viewModel.uiState.test {
            val state = awaitItem()
            state.state shouldBe State.Error()
        }
    }

})