package com.mskd.flux.features.gallery.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.gallery.domain.usecase.GetGalleryImagesUseCase
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestDispatcher

class GalleryViewModelTest : FunSpec({

    val testDispatcher: TestDispatcher = fluxExtensions()

    lateinit var viewModel: GalleryViewModel
    lateinit var apiRepository: ApiRepository
    lateinit var connectivityRepository: ConnectivityRepository

    val artwork = MediaMockups.movieArtwork

    val lowBackdrop = GalleryImage(path = "/low.jpg", type = GalleryImageType.BACKDROP, ratio = 1.78f, rating = 5.0)
    val highPoster = GalleryImage(path = "/high.jpg", type = GalleryImageType.POSTER, ratio = .67f, rating = 7.0)
    val midBackdrop = GalleryImage(path = "/mid.jpg", type = GalleryImageType.BACKDROP, ratio = 1.78f, rating = 6.0)

    val updateVm: () -> Unit = {
        viewModel = GalleryViewModel(
            artworkId = artwork.id,
            contentType = ContentType.MOVIE,
            getGalleryImages = GetGalleryImagesUseCase(apiRepository = apiRepository),
            databaseRepository = FakeDatabaseRepository(),
            connectivityRepository = connectivityRepository,
        )
    }

    beforeTest {
        apiRepository = mockk()
        connectivityRepository = mockk()
        every { connectivityRepository.currentlyOnline() } returns true
        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(lowBackdrop, highPoster, midBackdrop)
        updateVm()
    }

    test("loading then the images sorted by rating descending") {

        // Given : the images source suspends until the gate is completed
        val gate = CompletableDeferred<List<GalleryImage>>()
        coEvery { apiRepository.getMovieImages(artwork.id) } coAnswers { gate.await() }
        updateVm()

        // When / Then
        viewModel.uiState.test {

            awaitItem().state shouldBe State.Loading

            gate.complete(listOf(lowBackdrop, highPoster, midBackdrop))
            testDispatcher.scheduler.advanceUntilIdle()

            val content = (expectMostRecentItem().state as State.Content).content
            content.images shouldBe listOf(highPoster, midBackdrop, lowBackdrop)

        }

    }

    test("exposes the artwork title") {

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.title shouldBe artwork.title

    }

    fun contentImages(): List<GalleryImage> =
        (viewModel.uiState.value.state as State.Content).content.images

    fun contentViewerIndex(): Int? =
        (viewModel.uiState.value.state as State.Content).content.viewerIndex

    test("offline gives the unavailable state without requesting the images") {

        clearMocks(apiRepository, answers = false)
        every { connectivityRepository.currentlyOnline() } returns false
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.state shouldBe State.Error()
        viewModel.uiState.value.failure shouldBe GalleryFailure.OFFLINE
        coVerify(exactly = 0) { apiRepository.getMovieImages(any()) }

    }

    test("retry reloads the images once back online") {

        every { connectivityRepository.currentlyOnline() } returns false
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        every { connectivityRepository.currentlyOnline() } returns true
        viewModel.handleIntent(GalleryIntent.OnRetryTap)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.failure shouldBe null
        contentImages() shouldBe listOf(highPoster, midBackdrop, lowBackdrop)

    }

    test("a network error gives the error state and retry recovers") {

        coEvery { apiRepository.getMovieImages(artwork.id) } throws RuntimeException("boom")
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.state shouldBe State.Error()
        viewModel.uiState.value.failure shouldBe GalleryFailure.ERROR

        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(highPoster)
        viewModel.handleIntent(GalleryIntent.OnRetryTap)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.failure shouldBe null
        contentImages() shouldBe listOf(highPoster)

    }

    test("a failing retry keeps the images already displayed") {

        testDispatcher.scheduler.advanceUntilIdle()
        coEvery { apiRepository.getMovieImages(artwork.id) } throws RuntimeException("boom")

        viewModel.handleIntent(GalleryIntent.OnRetryTap)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.failure shouldBe GalleryFailure.ERROR
        contentImages() shouldBe listOf(highPoster, midBackdrop, lowBackdrop)

    }

    test("an empty list gives empty content") {

        coEvery { apiRepository.getMovieImages(artwork.id) } returns emptyList()
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.failure shouldBe null
        contentImages() shouldBe emptyList()

    }

    test("tapping an image opens the viewer on it and closing returns to the grid") {

        testDispatcher.scheduler.advanceUntilIdle()
        contentViewerIndex() shouldBe null

        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 1))
        testDispatcher.scheduler.advanceUntilIdle()
        contentViewerIndex() shouldBe 1

        viewModel.handleIntent(GalleryIntent.OnViewerClose)
        testDispatcher.scheduler.advanceUntilIdle()
        contentViewerIndex() shouldBe null

    }

    test("swiping the viewer updates the current index") {

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))
        viewModel.handleIntent(GalleryIntent.OnViewerPageChange(index = 2))
        testDispatcher.scheduler.advanceUntilIdle()

        contentViewerIndex() shouldBe 2

    }

    test("back closes the viewer first then leaves the gallery") {

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.event.test {

            viewModel.handleIntent(GalleryIntent.OnBackTap)
            testDispatcher.scheduler.advanceUntilIdle()
            contentViewerIndex() shouldBe null
            expectNoEvents()

            viewModel.handleIntent(GalleryIntent.OnBackTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe GalleryEvent.BackToPreviousScreen

        }

    }

    test("tapping an out of range image does not open the viewer") {

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 10))
        testDispatcher.scheduler.advanceUntilIdle()

        contentViewerIndex() shouldBe null

    }

})
