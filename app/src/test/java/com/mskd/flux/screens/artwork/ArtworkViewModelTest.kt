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
import com.mskd.flux.useCases.artwork.ArtworkUC
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

    val updateVm: () -> Unit = {

        progressUC = mockkProgressUC()

        viewModel = ArtworkViewModel(
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

            initialState.state.shouldBeInstanceOf<State.Content<FullArtwork>>()
            val content = (initialState.state as State.Content).content
            content.artwork shouldBe MediaMockups.showArtwork
            initialState.selectedMedia shouldBe MediaMockups.episode1
            (content as FullArtwork.FullShow).episodes shouldBe MediaMockups.episodes
            initialState.selectedSeason shouldBe MediaMockups.episode1.season
            initialState.episodePendingConfirmation shouldBe null

        }

    }

    test("select season") {

        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(ArtworkIntent.SelectSeason(2))

            val updatedState = expectMostRecentItem()

            updatedState.selectedSeason shouldBe 2

        }

    }

    test("show player") {

        viewModel.uiState.test {

            val initialState = expectMostRecentItem()
            val media = initialState.selectedMedia

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
            val media = initialState.selectedMedia

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
            val media = initialState.selectedMedia as Episode

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

            updatedState.episodePendingConfirmation shouldBe MediaMockups.episode2

            cancelAndConsumeRemainingEvents()

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
            val episodes = ((loadedState.state as State.Content).content as FullArtwork.FullShow).episodes

            // Change status of the latest episode
            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = episodes.last()))

            val stateWithDialog = expectMostRecentItem()
            stateWithDialog.episodePendingConfirmation shouldNotBe null

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

            initialState.selectedMedia.status shouldBe Status.TO_WATCH

            viewModel.handleIntent(ArtworkIntent.ChangeWatchStatus(media = initialState.selectedMedia))

            coVerify {
                progressUC.changeMediaStatus(
                    media = match { it.mediaId == initialState.selectedMedia.mediaId },
                    status = match { it == Status.WATCHED }
                )
            }

            cancelAndConsumeRemainingEvents()

        }

    }

    test("show reset progress dialog") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(ArtworkIntent.ShowResetProgressDialog(show = true))
            awaitItem().showResetProgressDialog shouldBe true

            viewModel.handleIntent(ArtworkIntent.ShowResetProgressDialog(show = false))
            awaitItem().showResetProgressDialog shouldBe false

        }
    }

    test("reset progress") {
        viewModel.uiState.test {
            val state = awaitItem()

            viewModel.handleIntent(ArtworkIntent.ResetProgress)

            val content = (state.state as State.Content).content
            coVerify { progressUC.resetProgress(content.artwork) }

        }
    }

})