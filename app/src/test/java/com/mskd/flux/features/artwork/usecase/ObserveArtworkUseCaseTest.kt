package com.mskd.flux.features.artwork.usecase

import app.cash.turbine.test
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCaseImpl
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class ObserveArtworkUseCaseTest : FunSpec({

    lateinit var database: DatabaseRepository
    lateinit var detailsRepository: DetailsRepository
    lateinit var sourcesUseCase: FlowSourcesUseCase
    lateinit var useCase: ObserveArtworkUseCaseImpl

    lateinit var artworkFlow: MutableStateFlow<Artwork?>
    lateinit var seasonsFlow: MutableStateFlow<List<Season>>
    lateinit var mediasFlow: MutableStateFlow<List<Media>>
    lateinit var sourcesFlow: MutableStateFlow<List<UserFolder>>

    beforeTest {
        artworkFlow = MutableStateFlow(null)
        seasonsFlow = MutableStateFlow(emptyList())
        mediasFlow = MutableStateFlow(emptyList())
        sourcesFlow = MutableStateFlow(emptyList())

        database = mockk()
        every { database.flowArtwork(any()) } returns artworkFlow
        every { database.flowSeasons(any()) } returns seasonsFlow
        every { database.flowMedias(any()) } returns mediasFlow

        detailsRepository = mockk(relaxed = true) {
            every { flowGenres() } returns MutableStateFlow(DetailsMockup.allGenres)
            coEvery { getGenresCount() } returns DetailsMockup.allGenres.count()
        }

        sourcesUseCase = mockk()
        every { sourcesUseCase() } returns sourcesFlow

        useCase = ObserveArtworkUseCaseImpl(
            database = database,
            detailsRepository = detailsRepository,
            sourcesUseCase = sourcesUseCase
        )

    }

    test("if no artwork, no flow") {
        runTest {
            useCase.flow.test {
                expectNoEvents()
            }
        }
    }

    test("if artwork is missing -> State.Error") {
        runTest {
            useCase.invoke(MediaMockups.movie.artworkId)

            useCase.flow.test {
                awaitItem().shouldBeInstanceOf<State.Error>()
            }
        }
    }

    test("if type MOVIE but movie is missing -> State.Error") {
        runTest {
            artworkFlow.value = MediaMockups.movieArtwork
            useCase.invoke(MediaMockups.movieArtwork.id)

            useCase.flow.test {
                awaitItem().shouldBeInstanceOf<State.Error>()
            }
        }
    }

    test("if type MOVIE with movie available -> State.Content(FullMovie)") {
        runTest {
            artworkFlow.value = MediaMockups.movieArtwork
            mediasFlow.value = listOf(MediaMockups.movie)
            useCase.invoke(MediaMockups.movieArtwork.id)

            useCase.flow.test {
                val content = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                val fullMovie = content.shouldBeInstanceOf<FullArtwork.FullMovie>()

                fullMovie.artwork shouldBe MediaMockups.movieArtwork
                fullMovie.movie shouldBe MediaMockups.movie.copy(isAvailable = true)
            }
        }
    }

    test("if type SHOW -> State.Content(FullShow) with filtered seasons") {
        runTest {
            artworkFlow.value = MediaMockups.showArtwork
            seasonsFlow.value = MediaMockups.seasons
            mediasFlow.value = MediaMockups.episodes
            useCase.invoke(MediaMockups.showArtwork.id)

            useCase.flow.test {
                val content = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                val fullShow = content.shouldBeInstanceOf<FullArtwork.FullShow>()

                fullShow.artwork shouldBe MediaMockups.showArtwork
                // episodes = [S1E1, S1E2, S2E33] -> saisons attendues = season1, season2 (season3 exclue)
                fullShow.seasons shouldBe listOf(MediaMockups.season1, MediaMockups.season2)
                fullShow.episodes shouldBe MediaMockups.episodes
            }
        }
    }

    test("new artworkID -> cancel previous observation and then start a new one") {
        runTest {
            val movieArtworkFlow = MutableStateFlow<Artwork?>(MediaMockups.movieArtwork)
            val showArtworkFlow = MutableStateFlow<Artwork?>(MediaMockups.showArtwork)

            every { database.flowArtwork(MediaMockups.movieArtwork.id) } returns movieArtworkFlow
            every { database.flowSeasons(MediaMockups.movieArtwork.id) } returns MutableStateFlow(emptyList())
            every { database.flowMedias(MediaMockups.movieArtwork.id) } returns MutableStateFlow(listOf(MediaMockups.movie))

            every { database.flowArtwork(MediaMockups.showArtwork.id) } returns showArtworkFlow
            every { database.flowSeasons(MediaMockups.showArtwork.id) } returns MutableStateFlow(MediaMockups.seasons)
            every { database.flowMedias(MediaMockups.showArtwork.id) } returns MutableStateFlow(MediaMockups.episodes)

            useCase.invoke(MediaMockups.movieArtwork.id)

            useCase.flow.test {
                val first = awaitItem().shouldBeInstanceOf<State.Content<FullArtwork>>()
                first.content.shouldBeInstanceOf<FullArtwork.FullMovie>()

                useCase.invoke(MediaMockups.showArtwork.id)

                val second = awaitItem().shouldBeInstanceOf<State.Content<FullArtwork>>()
                second.content.shouldBeInstanceOf<FullArtwork.FullShow>()
            }
        }
    }

    test("SAF file with folder available -> movie.isAvailable = true") {
        runTest {
            val safMovie = MediaMockups.movie.copy(
                file = MediaMockups.movie.file.copy(
                    path = "content://tree/primary/Movies/your_name.mkv",
                    source = FileSource.SAF
                )
            )

            artworkFlow.value = MediaMockups.movieArtwork
            mediasFlow.value = listOf(safMovie)
            sourcesFlow.value = listOf(
                UserFolder(path = "content://tree/primary/Movies", isAvailable = true)
            )
            useCase.invoke(MediaMockups.movieArtwork.id)

            useCase.flow.test {
                val fullMovie = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                    .shouldBeInstanceOf<FullArtwork.FullMovie>()

                fullMovie.movie.isAvailable shouldBe true
            }
        }
    }

    test("SAF file with folder unavailable -> movie.isAvailable = false") {
        runTest {
            val safMovie = MediaMockups.movie.copy(
                file = MediaMockups.movie.file.copy(
                    path = "content://tree/primary/Movies/your_name.mkv",
                    source = FileSource.SAF
                )
            )

            artworkFlow.value = MediaMockups.movieArtwork
            mediasFlow.value = listOf(safMovie)
            sourcesFlow.value = listOf(
                UserFolder(path = "content://tree/primary/Movies", isAvailable = false)
            )
            useCase.invoke(MediaMockups.movieArtwork.id)

            useCase.flow.test {
                val fullMovie = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                    .shouldBeInstanceOf<FullArtwork.FullMovie>()

                fullMovie.movie.isAvailable shouldBe false
            }
        }
    }

    test("SAF file without corresponding folder -> movie.isAvailable = false (default)") {
        runTest {
            val safMovie = MediaMockups.movie.copy(
                file = MediaMockups.movie.file.copy(
                    path = "content://tree/primary/Movies/your_name.mkv",
                    source = FileSource.SAF
                )
            )

            artworkFlow.value = MediaMockups.movieArtwork
            mediasFlow.value = listOf(safMovie)
            sourcesFlow.value = listOf(
                UserFolder(path = "content://tree/primary/OtherFolder", isAvailable = true)
            )
            useCase.invoke(MediaMockups.movieArtwork.id)

            useCase.flow.test {
                val fullMovie = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                    .shouldBeInstanceOf<FullArtwork.FullMovie>()

                fullMovie.movie.isAvailable shouldBe false
            }
        }
    }

    test("SAF files within a show with unavailable folder -> episodes.isAvailable = false") {
        runTest {
            val safEpisodes = MediaMockups.episodes.map { episode ->
                episode.copy(
                    file = episode.file.copy(
                        path = "content://tree/primary/Naruto/${episode.file.name}",
                        source = FileSource.SAF
                    )
                )
            }

            artworkFlow.value = MediaMockups.showArtwork
            seasonsFlow.value = MediaMockups.seasons
            mediasFlow.value = safEpisodes
            sourcesFlow.value = listOf(
                UserFolder(path = "content://tree/primary/Naruto", isAvailable = false)
            )
            useCase.invoke(MediaMockups.showArtwork.id)

            useCase.flow.test {
                val fullShow = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                    .shouldBeInstanceOf<FullArtwork.FullShow>()

                fullShow.episodes.forEach { it.isAvailable shouldBe false }
            }
        }
    }

    test("SAF files: a change of isAvailable on the corresponding folder reemits (distinctUntilChanged true)") {
        runTest {
            val safMovie = MediaMockups.movie.copy(
                file = MediaMockups.movie.file.copy(
                    path = "content://tree/primary/Movies/your_name.mkv",
                    source = FileSource.SAF
                )
            )

            artworkFlow.value = MediaMockups.movieArtwork
            mediasFlow.value = listOf(safMovie)
            sourcesFlow.value = listOf(
                UserFolder(path = "content://tree/primary/Movies", isAvailable = true)
            )
            useCase.invoke(MediaMockups.movieArtwork.id)

            useCase.flow.test {
                val first = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                    .shouldBeInstanceOf<FullArtwork.FullMovie>()
                first.movie.isAvailable shouldBe true

                sourcesFlow.value = listOf(
                    UserFolder(path = "content://tree/primary/Movies", isAvailable = false)
                )

                val second = awaitItem()
                    .shouldBeInstanceOf<State.Content<FullArtwork>>()
                    .content
                    .shouldBeInstanceOf<FullArtwork.FullMovie>()
                second.movie.isAvailable shouldBe false
            }
        }
    }

})