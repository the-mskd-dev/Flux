package com.mskd.flux.features.movie.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.fake.FakeObserveArtworkUseCase
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import com.mskd.flux.features.people.domain.usecase.GetPeopleUseCase
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.system.FilesLauncher
import com.mskd.flux.system.UrlLauncher
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class MovieViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: MovieViewModel
    lateinit var tokenDataStore: TokenDataStore
    lateinit var filesLauncher: FilesLauncher
    lateinit var urlLauncher: UrlLauncher
    lateinit var observeArtworkUseCase: ObserveArtworkUseCase
    lateinit var getPeopleUseCase: GetPeopleUseCase
    lateinit var changeMediaStatus: ChangeMediaStatusUseCase
    lateinit var resetProgress: ResetProgressUseCase
    lateinit var resolvePlaybackAction: ResolvePlaybackActionUseCase
    lateinit var recordPlaybackResult: SaveProgressUseCase
    lateinit var token: MutableStateFlow<String>
    lateinit var syncState: MutableStateFlow<SyncState>

    val movie = MediaMockups.movie
    val movieArtwork = MediaMockups.movieArtwork

    val updateVm: (id: Long) -> Unit = { id ->

        filesLauncher = mockk(relaxed = true)
        urlLauncher = mockk(relaxed = true)
        changeMediaStatus = mockk(relaxed = true)
        resetProgress = mockk(relaxed = true)
        resolvePlaybackAction = mockk(relaxed = true)
        recordPlaybackResult = mockk(relaxed = true)

        viewModel = MovieViewModel(
            artworkId = id,
            observeArtworkUseCase = observeArtworkUseCase,
            getPeopleUseCase = getPeopleUseCase,
            tokenDataStore = tokenDataStore,
            catalogSyncCoordinator = mockk<CatalogSyncCoordinator> { every { state } returns syncState },
            changeMediaStatus = changeMediaStatus,
            resetProgress = resetProgress,
            resolvePlaybackAction = resolvePlaybackAction,
            recordPlaybackResult = recordPlaybackResult,
            filesLauncher = filesLauncher,
            urlLauncher = urlLauncher
        )

    }

    suspend fun content(): MovieContent =
        (viewModel.uiState.first { it.state is State.Content }.state as State.Content).content

    beforeTest {

        observeArtworkUseCase = FakeObserveArtworkUseCase()
        getPeopleUseCase = mockk()
        every { getPeopleUseCase(any()) } returns MutableStateFlow(emptyList<Person>().toImmutableList())

        token = MutableStateFlow("token")
        syncState = MutableStateFlow(SyncState.Idle())
        tokenDataStore = mockk(relaxed = true) {
            coEvery { tokenIsAvailable() } returns true
            every { flow } returns token
        }

        updateVm(movieArtwork.id)

    }

    context("loading") {

        test("loading until the Artwork is observed") {
            val artwork = MutableSharedFlow<State<FullArtwork>>()
            observeArtworkUseCase = mockk { every { this@mockk(any()) } returns artwork }
            updateVm(movieArtwork.id)

            viewModel.uiState.test {
                awaitItem().state shouldBe State.Loading
                artwork.emit(State.Content(MediaMockups.fullMovie))
                awaitItem().state.shouldBeInstanceOf<State.Content<MovieContent>>()
            }
        }

        test("the movie is shown with its description and genres") {
            val content = content()
            content.fullMovie.artwork shouldBe movieArtwork
            content.fullMovie.movie shouldBe movie
            content.overview shouldBe ArtworkOverviewContent.MovieOverview(movie = movie, genres = content.fullMovie.genres)
            content.dialog shouldBe null
        }

        test("an unknown Artwork is an error") {
            updateVm(-999L)
            viewModel.uiState.test {
                awaitItem().state shouldBe State.Error()
            }
        }

        test("a series is an error: it has its own page") {
            updateVm(MediaMockups.showArtwork.id)
            viewModel.uiState.test {
                awaitItem().state shouldBe State.Error()
            }
        }

    }

    context("people") {

        test("the people of the movie are shown") {
            val people = listOf(
                Person(
                    id = 819,
                    creditId = "credit-819",
                    artworkId = movieArtwork.id,
                    name = "Edward Norton",
                    profilePath = "/profile.jpg",
                    role = "The Narrator",
                    type = PersonType.CAST,
                    personOrder = 0
                )
            ).toImmutableList()
            every { getPeopleUseCase(movieArtwork.id) } returns MutableStateFlow(people)
            updateVm(movieArtwork.id)

            content().people shouldBe people
        }

        test("a tap on a person opens their page") {
            val person = PeopleMockup.cast.first()
            viewModel.event.test {
                viewModel.handleIntent(MovieIntent.OnPersonClick(person = person))
                awaitItem() shouldBe MovieEvent.NavigateToPerson(person = person)
            }
        }

    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(MovieIntent.OnBackTap)
            awaitItem() shouldBe MovieEvent.BackToPreviousScreen
        }
    }

    test("PlayMedia - should call resolvePlaybackAction and then launch player event") {

        checkAll(Exhaustive.boolean(), Exhaustive.boolean()) { forceInternal, externalPlayerRequested ->

            // Given
            val externalPlayer = !forceInternal && externalPlayerRequested
            coEvery { resolvePlaybackAction(media = movie, forceInternal = forceInternal) } returns PlaybackAction.OpenPlayer(media = movie, externalPlayer = externalPlayer)

            viewModel.event.test {

                // When
                viewModel.handleIntent(intent = MovieIntent.PlayMedia(media = movie, forceInternal = forceInternal))

                // Then
                awaitItem() shouldBe MovieEvent.PlayMedia(media = movie, externalPlayer = externalPlayer)

            }

        }

    }

    test("the progress returned by the external player is saved for the movie") {
        content()
        viewModel.handleIntent(MovieIntent.OnExternalPlayerResult(progress = 5000L))
        coVerify { recordPlaybackResult(media = movie, progress = 5000L) }
    }

    test("mark movie as watched") {
        movie.status shouldBe Status.TO_WATCH
        content()

        viewModel.handleIntent(MovieIntent.ChangeWatchStatus(media = movie))

        coVerify { changeMediaStatus(media = movie, status = Status.WATCHED) }
    }

    context("reset progress") {

        test("the dialog opens and closes") {
            viewModel.uiState.test {
                awaitItem()

                viewModel.handleIntent(MovieIntent.ShowResetProgressDialog)
                (awaitItem().state as State.Content).content.dialog shouldBe MoviePageDialog.ResetProgressConfirmation

                viewModel.handleIntent(MovieIntent.CloseDialog)
                (awaitItem().state as State.Content).content.dialog shouldBe null
            }
        }

        test("the progress of the movie is reset, and the dialog closes") {
            viewModel.uiState.test {
                awaitItem()
                viewModel.handleIntent(MovieIntent.ShowResetProgressDialog)
                awaitItem()

                viewModel.handleIntent(MovieIntent.ResetProgress)

                (awaitItem().state as State.Content).content.dialog shouldBe null
                coVerify { resetProgress(artwork = movieArtwork, season = null) }
            }
        }

    }

    test("OpenMovieInfo opens the TMDB page of the movie") {
        content()
        viewModel.handleIntent(MovieIntent.OpenMovieInfo)
        verify { urlLauncher.open(url = movieArtwork.infoUrl) }
    }

    test("OpenFileExplorer opens the folder of the movie file") {
        viewModel.handleIntent(MovieIntent.OpenFileExplorer(media = movie))
        verify { filesLauncher.open(file = movie.file) }
    }

    context("gallery") {

        test("a tap on the Artwork image opens the gallery of the movie") {
            content()
            viewModel.event.test {
                viewModel.handleIntent(MovieIntent.OnArtworkImageClick)
                awaitItem() shouldBe MovieEvent.NavigateToGallery(artworkId = movieArtwork.id, contentType = ContentType.MOVIE)
            }
        }

        test("does nothing without token") {
            coEvery { tokenDataStore.tokenIsAvailable() } returns false
            content()
            viewModel.event.test {
                viewModel.handleIntent(MovieIntent.OnArtworkImageClick)
                expectNoEvents()
            }
        }

    }

    context("Manual identification") {

        fun movieWith(fileName: String) {
            val namedMovie = movie.copy(file = movie.file.copy(name = fileName))
            observeArtworkUseCase = mockk {
                every { this@mockk(any()) } returns flowOf(State.Content(FullArtwork.FullMovie(artwork = movieArtwork, movie = namedMovie, genres = persistentListOf())))
            }
            updateVm(movieArtwork.id)
        }

        test("offered with a TMDB token") {
            content().identification shouldBe IdentificationAvailability.ENABLED
        }

        test("hidden without TMDB token") {
            token.value = ""
            updateVm(movieArtwork.id)
            content().identification shouldBe IdentificationAvailability.HIDDEN
        }

        test("offered when the file name carries a TMDB ID") {
            movieWith(fileName = "Your Name {tmdb-372058}.mkv")
            content().identification shouldBe IdentificationAvailability.ENABLED
        }

        test("disabled during a Sync, enabled again once it ends") {
            syncState.value = SyncState.Syncing(full = false)
            updateVm(movieArtwork.id)
            viewModel.uiState.test {
                (awaitItem().state as State.Content).content.identification shouldBe IdentificationAvailability.DISABLED
                syncState.value = SyncState.Idle()
                (awaitItem().state as State.Content).content.identification shouldBe IdentificationAvailability.ENABLED
            }
        }

        test("the dialog opens only when the option is enabled") {
            syncState.value = SyncState.Syncing(full = false)
            updateVm(movieArtwork.id)
            viewModel.uiState.test {
                awaitItem()
                viewModel.handleIntent(MovieIntent.ShowIdentificationDialog)
                expectNoEvents()

                syncState.value = SyncState.Idle()
                awaitItem()
                viewModel.handleIntent(MovieIntent.ShowIdentificationDialog)
                (awaitItem().state as State.Content).content.dialog shouldBe MoviePageDialog.Identification
            }
        }

        test("once identified as a movie, the dialog closes and the new Artwork is sent with its type") {
            viewModel.uiState.test {
                awaitItem()
                viewModel.handleIntent(MovieIntent.ShowIdentificationDialog)
                awaitItem()
                viewModel.event.test {
                    viewModel.handleIntent(MovieIntent.OnIdentified(artworkId = 42L, type = ContentType.MOVIE))
                    awaitItem() shouldBe MovieEvent.Identified(artworkId = 42L, type = ContentType.MOVIE)
                }
                (awaitItem().state as State.Content).content.dialog shouldBe null
            }
        }

        test("identified as a series, the new Artwork is sent as a series") {
            viewModel.event.test {
                viewModel.handleIntent(MovieIntent.OnIdentified(artworkId = 42L, type = ContentType.SHOW))
                awaitItem() shouldBe MovieEvent.Identified(artworkId = 42L, type = ContentType.SHOW)
            }
        }

    }

})
