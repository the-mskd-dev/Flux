package com.mskd.flux.screens.artwork

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.mockups.FakeArtworkUC
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.mockkProgressUC
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.useCases.progress.ProgressUC
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: ArtworkViewModel
    lateinit var settingsRepository: SettingsRepository
    lateinit var artworkUC: FakeArtworkUC
    lateinit var progressUC: ProgressUC
    var currentSeason: Int? = 1

    val updateVm: () -> Unit = {

        progressUC = mockkProgressUC()

        viewModel = ArtworkViewModel(
            artworkId = MediaMockups.showArtwork.id,
            season = currentSeason,
            artworkUC = artworkUC,
            settingsRepository = settingsRepository,
            progressUC = progressUC
        )

    }

    beforeTest {

        currentSeason = 1

        artworkUC = FakeArtworkUC(initialContentType = ContentType.SHOW)

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State())
        }

        updateVm()

    }

    test("initial state") {

        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.state.shouldBeInstanceOf<State.Content<ArtworkContent>>()
            val content = (initialState.state as State.Content).content
            content.fullArtwork.artwork shouldBe MediaMockups.showArtwork
            content.selectedMedia shouldBe MediaMockups.episode1
            (content.fullArtwork as FullArtwork.FullShow).episodes shouldBe MediaMockups.episodes
            content.selectedSeason shouldBe MediaMockups.episode1.season
            content.dialog shouldBe null

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
        updateVm()

        viewModel.uiState.test {

            val state = awaitItem()
            val content = (state.state as State.Content).content
            content.selectedSeason shouldBe 2

        }

    }

    test("show player") {

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val content = (initialState.state as State.Content).content
            val media = content.selectedMedia

            viewModel.event.test {

                viewModel.handleIntent(ArtworkIntent.PlayMedia(media = media))

                val event = awaitItem()

                event.shouldBeInstanceOf<ArtworkEvent.PlayMedia>()
                event.mediaId shouldBe media.mediaId

            }

        }

    }

    test("show external player") {

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsRepository.State(externalPlayer = true))
        }

        updateVm()

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val content = (initialState.state as State.Content).content
            val media = content.selectedMedia

            viewModel.event.test {

                viewModel.handleIntent(ArtworkIntent.PlayMedia(media = media))

                val event = awaitItem()

                event.shouldBeInstanceOf<ArtworkEvent.LaunchExternalPlayer>()
                event.media shouldBe media

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
                progressUC.changeMediaStatus(
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
                progressUC.changeMediaStatus(
                    media = match { it.mediaId == MediaMockups.episode1.mediaId },
                    status = match { it == Status.WATCHED }
                )
            }

            // Change status of episode 2
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = MediaMockups.episode2))

            coVerify {
                progressUC.changeMediaStatus(
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


            coVerify { progressUC.markPreviousEpisodesAsWatchedFor(episode = episodes.last()) }

            cancelAndConsumeRemainingEvents()

        }

    }

    test("mark movie as watched") {

        artworkUC.setContent(
            State.Content(
                FullArtwork.FullMovie(
                    resume = MediaMockups.movieArtwork,
                    movie = MediaMockups.movie
                )
            )
        )

        updateVm()

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val content = (initialState.state as State.Content).content
            val media = content.selectedMedia

            media.status shouldBe Status.TO_WATCH

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = media))

            coVerify {
                progressUC.changeMediaStatus(
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

            coVerify { progressUC.resetProgress(content.fullArtwork.artwork, 1) }

        }
    }

    test("open artwork info") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.event.test {
                viewModel.handleIntent(ArtworkIntent.OpenArtworkInfo)
                val event = awaitItem()
                event.shouldBeInstanceOf<ArtworkEvent.OpenUrlInfo>()
                event.url shouldBe MediaMockups.season1.infoUrl
            }
        }
    }

    test("open episode info") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.event.test {
                val episode = MediaMockups.episode1
                viewModel.handleIntent(ArtworkIntent.OpenEpisodeInfo(episode))
                val event = awaitItem()
                event.shouldBeInstanceOf<ArtworkEvent.OpenUrlInfo>()
                event.url shouldBe episode.infoUrl
            }
        }
    }

    test("on external player result") {
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.handleIntent(ArtworkIntent.PlayMedia(MediaMockups.episode1))
            viewModel.handleIntent(ArtworkIntent.OnExternalPlayerResult(progress = 5000L))
            coVerify { progressUC.saveProgress(media = MediaMockups.episode1, progress = 5000L) }
        }
    }

    test("error state") {
        artworkUC = FakeArtworkUC(initialContentType = ContentType.SHOW)

        viewModel = ArtworkViewModel(
            artworkId = -999L,
            season = null,
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