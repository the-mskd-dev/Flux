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
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestDispatcher

class GalleryViewModelTest : FunSpec({

    val testDispatcher: TestDispatcher = fluxExtensions()

    lateinit var viewModel: GalleryViewModel
    lateinit var apiRepository: ApiRepository

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
        )
    }

    beforeTest {
        apiRepository = mockk()
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

})
