package com.mskd.flux.features.identification.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.network.tmdb.domain.model.ArtworkCandidate
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.identification.domain.usecase.IdentifyManuallyUseCase
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestDispatcher
import java.io.IOException

class IdentificationViewModelTest : FunSpec({

    val testDispatcher: TestDispatcher = fluxExtensions()

    lateinit var viewModel: IdentificationViewModel
    lateinit var databaseRepository: DatabaseRepository
    lateinit var apiRepository: ApiRepository
    lateinit var identifyManually: IdentifyManuallyUseCase

    val source = MediaMockups.movieArtwork
    val remake = Artwork(id = 42L, title = "Your Name (remake)", type = ContentType.MOVIE, imagePath = "/remake.jpg")
    val current = IdentificationCandidate(artwork = source, year = 2016, isCurrent = true)
    val other = IdentificationCandidate(artwork = remake, year = 2027, isCurrent = false)

    val updateVm: () -> Unit = {
        viewModel = IdentificationViewModel(
            artworkId = source.id,
            databaseRepository = databaseRepository,
            apiRepository = apiRepository,
            identifyManually = identifyManually,
        )
    }

    beforeTest {
        databaseRepository = mockk {
            coEvery { getArtwork(source.id) } returns source
            coEvery { getMovie(source.id) } returns MediaMockups.movie
        }
        apiRepository = mockk {
            coEvery { searchArtworks(title = any(), type = ContentType.MOVIE) } returns listOf(
                ArtworkCandidate(artwork = source, year = 2016),
                ArtworkCandidate(artwork = remake, year = 2027),
            )
        }
        identifyManually = mockk()
        updateVm()
    }

    test("the search is prefilled with the title of the file name, searched without year, and marks the current Artwork") {

        viewModel.uiState.value shouldBe IdentificationUiState(
            query = "your name",
            results = State.Content(persistentListOf(current, other)),
        )
        coVerify(exactly = 1) { apiRepository.searchArtworks(title = "your name", type = ContentType.MOVIE) }

    }

    test("loading while the search runs") {

        // Given
        val gate = CompletableDeferred<List<ArtworkCandidate>>()
        coEvery { apiRepository.searchArtworks(any(), any()) } coAnswers { gate.await() }
        updateVm()

        // When / Then
        viewModel.uiState.test {
            awaitItem().results shouldBe State.Loading
            gate.complete(emptyList())
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem().results shouldBe State.Content(persistentListOf())
        }

    }

    test("an edited query is searched on submit") {

        // Given
        coEvery { apiRepository.searchArtworks(title = "kimi no na wa", type = ContentType.MOVIE) } returns listOf(ArtworkCandidate(artwork = remake, year = 2027))

        // When
        viewModel.handleIntent(IdentificationIntent.UpdateQuery("kimi no na wa"))
        viewModel.handleIntent(IdentificationIntent.Search)

        // Then
        viewModel.uiState.value shouldBe IdentificationUiState(query = "kimi no na wa", results = State.Content(persistentListOf(other)))

    }

    test("no result is an empty list") {

        // Given
        coEvery { apiRepository.searchArtworks(any(), any()) } returns emptyList()

        // When
        updateVm()

        // Then
        viewModel.uiState.value.results shouldBe State.Content(persistentListOf())

    }

    test("a failed search is an error, and retrying searches again") {

        // Given
        coEvery { apiRepository.searchArtworks(any(), any()) } throws IOException("network down")
        updateVm()
        viewModel.uiState.value.results shouldBe State.Error()

        // When
        coEvery { apiRepository.searchArtworks(any(), any()) } returns listOf(ArtworkCandidate(artwork = remake, year = 2027))
        viewModel.handleIntent(IdentificationIntent.Search)

        // Then
        viewModel.uiState.value.results shouldBe State.Content(persistentListOf(other))

    }

    test("choosing a result identifies the Artwork, loading meanwhile, then sends the new Artwork") {

        // Given
        val gate = CompletableDeferred<Result<Long>>()
        coEvery { identifyManually(artworkId = source.id, target = remake) } coAnswers { gate.await() }

        viewModel.event.test {

            // When
            viewModel.handleIntent(IdentificationIntent.Select(other))
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            viewModel.uiState.value.isIdentifying shouldBe true
            gate.complete(Result.success(remake.id))
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe IdentificationEvent.Identified(artworkId = remake.id)

        }

    }

    test("a failed identification stops loading and is reported, the results stay choosable") {

        // Given
        coEvery { identifyManually(artworkId = source.id, target = remake) } returns Result.failure(IOException("network down"))

        // When
        viewModel.handleIntent(IdentificationIntent.Select(other))

        // Then
        viewModel.uiState.value shouldBe IdentificationUiState(
            query = "your name",
            results = State.Content(persistentListOf(current, other)),
            isIdentifying = false,
            identificationFailed = true,
        )

    }

    test("the current Artwork cannot be chosen") {

        // When
        viewModel.handleIntent(IdentificationIntent.Select(current))

        // Then
        viewModel.uiState.value.isIdentifying shouldBe false
        coVerify(exactly = 0) { identifyManually(any(), any()) }

    }

})
