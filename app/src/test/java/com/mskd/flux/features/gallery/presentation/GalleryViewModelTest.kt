package com.mskd.flux.features.gallery.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.domain.usecase.GetGalleryImagesUseCase
import com.mskd.flux.features.gallery.domain.usecase.ImageDownloadUseCase
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.system.FilesLauncher
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher

class GalleryViewModelTest : FunSpec({

    val testDispatcher: TestDispatcher = fluxExtensions()

    lateinit var viewModel: GalleryViewModel
    lateinit var apiRepository: ApiRepository
    lateinit var connectivityRepository: ConnectivityRepository
    lateinit var imageDownload: ImageDownloadUseCase
    lateinit var filesLauncher: FilesLauncher
    lateinit var databaseRepository: DatabaseRepository
    lateinit var storedArtwork: MutableStateFlow<Artwork?>
    lateinit var syncState: MutableStateFlow<SyncState>

    val artwork = MediaMockups.movieArtwork

    val lowBackdrop = GalleryImage(path = "/low.jpg", type = GalleryImageType.BACKDROP, ratio = 1.78f, rating = 5.0)
    val highPoster = GalleryImage(path = "/high.jpg", type = GalleryImageType.POSTER, ratio = .67f, rating = 7.0)
    val midBackdrop = GalleryImage(path = "/mid.jpg", type = GalleryImageType.BACKDROP, ratio = 1.78f, rating = 6.0)

    fun createVm(artworkId: Long, contentType: ContentType) {
        viewModel = GalleryViewModel(
            artworkId = artworkId,
            contentType = contentType,
            getGalleryImages = GetGalleryImagesUseCase(apiRepository = apiRepository),
            databaseRepository = databaseRepository,
            connectivityRepository = connectivityRepository,
            imageDownload = imageDownload,
            filesLauncher = filesLauncher,
            catalogSyncCoordinator = mockk { every { state } returns syncState },
        )
    }

    val updateVm: () -> Unit = { createVm(artworkId = artwork.id, contentType = ContentType.MOVIE) }

    beforeTest {
        storedArtwork = MutableStateFlow(artwork)
        syncState = MutableStateFlow(SyncState.Idle())
        // The Catalog: the stored Artwork is observed, and the targeted writes change it
        databaseRepository = mockk {
            every { flowArtwork(any()) } returns storedArtwork
            coEvery { getArtwork(any()) } answers { storedArtwork.value }
            coEvery { setArtworkCustomBackdrop(any(), any()) } answers { storedArtwork.update { it?.copy(customBackdropPath = secondArg()) } }
            coEvery { setArtworkCustomPoster(any(), any()) } answers { storedArtwork.update { it?.copy(customPosterPath = secondArg()) } }
            coEvery { setArtworkCustomLogo(any(), any()) } answers { storedArtwork.update { it?.copy(customLogoPath = secondArg()) } }
        }
        apiRepository = mockk()
        connectivityRepository = mockk()
        imageDownload = mockk()
        filesLauncher = mockk(relaxed = true)
        coEvery { imageDownload(any(), any()) } returns Unit
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

    fun contentRunning(): Boolean =
        (viewModel.uiState.value.state as State.Content).content.imageDownloadRunning

    test("downloading from the viewer downloads the image at the viewer index with the artwork title") {

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 1))
        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { imageDownload(midBackdrop, artwork.title) }

    }

    test("after a swipe the download targets the new current image") {

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))
        viewModel.handleIntent(GalleryIntent.OnViewerPageChange(index = 2))
        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { imageDownload(lowBackdrop, artwork.title) }

    }

    test("the running flag is true during the download then false") {

        val gate = CompletableDeferred<Unit>()
        coEvery { imageDownload(any(), any()) } coAnswers { gate.await() }
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))
        contentRunning() shouldBe false

        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
        testDispatcher.scheduler.advanceUntilIdle()
        contentRunning() shouldBe true

        gate.complete(Unit)
        testDispatcher.scheduler.advanceUntilIdle()
        contentRunning() shouldBe false

    }

    test("a successful download emits the succeeded event") {

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))

        viewModel.event.test {
            viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe GalleryEvent.ImageDownloadSucceeded
        }

    }

    test("showing the downloaded image opens the downloaded images folder") {

        viewModel.handleIntent(GalleryIntent.OnShowDownloadedImageTap)

        verify(exactly = 1) { filesLauncher.openDownloadedImages() }

    }

    test("a failing download emits the failed event and stops running") {

        coEvery { imageDownload(any(), any()) } throws RuntimeException("boom")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))

        viewModel.event.test {
            viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe GalleryEvent.ImageDownloadFailed
        }
        contentRunning() shouldBe false

    }

    test("a cancelled download is not reported") {

        coEvery { imageDownload(any(), any()) } throws CancellationException("cancelled")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))

        viewModel.event.test {
            viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
            testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
        }

    }

    test("a second tap while a download runs is ignored") {

        val gate = CompletableDeferred<Unit>()
        coEvery { imageDownload(any(), any()) } coAnswers { gate.await() }
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))

        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
        gate.complete(Unit)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { imageDownload(any(), any()) }

    }

    test("a tap with the viewer closed does nothing") {

        testDispatcher.scheduler.advanceUntilIdle()
        val before = viewModel.uiState.value

        viewModel.event.test {
            viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
            testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
        }

        coVerify(exactly = 0) { imageDownload(any(), any()) }
        viewModel.uiState.value shouldBe before

    }

    test("a swipe does not cancel the running download") {

        val gate = CompletableDeferred<Unit>()
        coEvery { imageDownload(any(), any()) } coAnswers { gate.await() }
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))
        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.event.test {
            viewModel.handleIntent(GalleryIntent.OnViewerPageChange(index = 1))
            gate.complete(Unit)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe GalleryEvent.ImageDownloadSucceeded
        }

    }

    test("the rest of the state is untouched by a download") {

        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 1))
        val before = viewModel.uiState.value

        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value shouldBe before
        contentImages() shouldBe listOf(highPoster, midBackdrop, lowBackdrop)
        contentViewerIndex() shouldBe 1

    }

    test("a retry while a download runs keeps the running flag") {

        val gate = CompletableDeferred<Unit>()
        coEvery { imageDownload(any(), any()) } coAnswers { gate.await() }
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))
        viewModel.handleIntent(GalleryIntent.OnImageDownloadTap)

        viewModel.handleIntent(GalleryIntent.OnRetryTap)
        testDispatcher.scheduler.advanceUntilIdle()

        contentRunning() shouldBe true

    }

    test("a show requests only the show images, sorted by rating descending") {

        val show = MediaMockups.showArtwork
        // The movie view model of beforeTest has already loaded its images
        clearMocks(apiRepository, answers = false)
        coEvery { apiRepository.getShowImages(show.id) } returns listOf(lowBackdrop, highPoster, midBackdrop)
        storedArtwork.value = show
        createVm(artworkId = show.id, contentType = ContentType.SHOW)
        testDispatcher.scheduler.advanceUntilIdle()

        contentImages() shouldBe listOf(highPoster, midBackdrop, lowBackdrop)
        coVerify(exactly = 0) { apiRepository.getMovieImages(any()) }

    }

    //region Logo

    val topLogo = GalleryImage(path = "/logo.png", type = GalleryImageType.LOGO, ratio = 2.5f, rating = 9.0)

    test("logos are mixed with backdrops and posters, sorted by rating") {

        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(lowBackdrop, topLogo, highPoster)
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        contentImages() shouldBe listOf(topLogo, highPoster, lowBackdrop)

    }

    test("the viewer opens on a logo that can be used") {

        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(lowBackdrop, topLogo)
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))

        contentViewerIndex() shouldBe 0
        (viewModel.uiState.value.state as State.Content).content.artworkImageButton shouldBe ArtworkImageButton.AVAILABLE

    }

    test("using a logo saves it as custom Logo and it becomes the current Logo") {

        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(lowBackdrop, topLogo)
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        val images = contentImages()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = images.indexOf(topLogo)))
        viewModel.handleIntent(GalleryIntent.OnUseImageTap)
        testDispatcher.scheduler.advanceUntilIdle()

        storedArtwork.value?.customLogoPath shouldBe topLogo.path
        (viewModel.uiState.value.state as State.Content).content.artworkImageButton shouldBe ArtworkImageButton.CURRENT

    }

    test("the automatic Logo is the current Logo") {

        storedArtwork.value = artwork.copy(logoPath = topLogo.path)
        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(topLogo)
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))

        (viewModel.uiState.value.state as State.Content).content.artworkImageButton shouldBe ArtworkImageButton.CURRENT

    }

    test("with a custom Logo, the automatic Logo is available again") {

        val autoLogo = GalleryImage(path = "/auto.png", type = GalleryImageType.LOGO, ratio = 2f, rating = 3.0)
        storedArtwork.value = artwork.copy(logoPath = autoLogo.path, customLogoPath = topLogo.path)
        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(topLogo, autoLogo)
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()

        val images = contentImages()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = images.indexOf(topLogo)))
        (viewModel.uiState.value.state as State.Content).content.artworkImageButton shouldBe ArtworkImageButton.CURRENT
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = images.indexOf(autoLogo)))
        (viewModel.uiState.value.state as State.Content).content.artworkImageButton shouldBe ArtworkImageButton.AVAILABLE

    }

    test("the logo button is hidden while a Sync runs") {

        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(topLogo)
        updateVm()
        testDispatcher.scheduler.advanceUntilIdle()
        syncState.value = SyncState.Syncing(full = false)

        viewModel.handleIntent(GalleryIntent.OnImageTap(index = 0))

        (viewModel.uiState.value.state as State.Content).content.artworkImageButton shouldBe ArtworkImageButton.HIDDEN

    }

    //endregion

    //region Custom image

    fun buttonOf(path: String): ArtworkImageButton {
        val images = contentImages()
        viewModel.handleIntent(GalleryIntent.OnImageTap(index = images.indexOfFirst { it.path == path }))
        return (viewModel.uiState.value.state as State.Content).content.artworkImageButton
    }

    fun useImage(path: String) {
        buttonOf(path) shouldBe ArtworkImageButton.AVAILABLE
        viewModel.handleIntent(GalleryIntent.OnUseImageTap)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    val tmdbBackdrop = GalleryImage(path = artwork.bannerPath, type = GalleryImageType.BACKDROP, ratio = 1.78f, rating = 4.0)
    val tmdbPoster = GalleryImage(path = artwork.imagePath, type = GalleryImageType.POSTER, ratio = .67f, rating = 3.0)

    fun showGallery() {
        val show = MediaMockups.showArtwork
        coEvery { apiRepository.getShowImages(show.id) } returns listOf(lowBackdrop, highPoster)
        storedArtwork.value = show
        createVm(artworkId = show.id, contentType = ContentType.SHOW)
    }

    test("on a Movie, both a Backdrop and a Poster can be used") {

        buttonOf(midBackdrop.path) shouldBe ArtworkImageButton.AVAILABLE
        buttonOf(highPoster.path) shouldBe ArtworkImageButton.AVAILABLE

    }

    test("on a Show, both a Backdrop and a Poster can be used") {

        showGallery()

        buttonOf(lowBackdrop.path) shouldBe ArtworkImageButton.AVAILABLE
        buttonOf(highPoster.path) shouldBe ArtworkImageButton.AVAILABLE

    }

    test("without Custom image, the TMDB Backdrop and the TMDB Poster are the current images") {

        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(lowBackdrop, tmdbBackdrop, tmdbPoster, highPoster)
        updateVm()

        buttonOf(tmdbBackdrop.path) shouldBe ArtworkImageButton.CURRENT
        buttonOf(tmdbPoster.path) shouldBe ArtworkImageButton.CURRENT
        buttonOf(lowBackdrop.path) shouldBe ArtworkImageButton.AVAILABLE
        buttonOf(highPoster.path) shouldBe ArtworkImageButton.AVAILABLE

    }

    test("with a custom Backdrop, it is the current Backdrop and the TMDB Backdrop becomes available") {

        storedArtwork.value = artwork.copy(customBackdropPath = lowBackdrop.path)
        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(lowBackdrop, tmdbBackdrop)
        updateVm()

        buttonOf(lowBackdrop.path) shouldBe ArtworkImageButton.CURRENT
        buttonOf(tmdbBackdrop.path) shouldBe ArtworkImageButton.AVAILABLE

    }

    test("with a custom Poster, it is the current Poster and the TMDB Poster becomes available") {

        storedArtwork.value = artwork.copy(customPosterPath = highPoster.path)
        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(highPoster, tmdbPoster)
        updateVm()

        buttonOf(highPoster.path) shouldBe ArtworkImageButton.CURRENT
        buttonOf(tmdbPoster.path) shouldBe ArtworkImageButton.AVAILABLE

    }

    test("the button is hidden while a Sync runs") {

        syncState.value = SyncState.Syncing(full = false)

        buttonOf(midBackdrop.path) shouldBe ArtworkImageButton.HIDDEN
        buttonOf(highPoster.path) shouldBe ArtworkImageButton.HIDDEN

        syncState.value = SyncState.Idle()

        buttonOf(midBackdrop.path) shouldBe ArtworkImageButton.AVAILABLE

    }

    test("the button is hidden with the viewer closed") {

        (viewModel.uiState.value.state as State.Content).content.artworkImageButton shouldBe ArtworkImageButton.HIDDEN

    }

    test("using a Backdrop saves it as custom Backdrop and it becomes the current Backdrop") {

        useImage(midBackdrop.path)

        storedArtwork.value?.customBackdropPath shouldBe midBackdrop.path
        storedArtwork.value?.customPosterPath shouldBe null
        buttonOf(midBackdrop.path) shouldBe ArtworkImageButton.CURRENT

    }

    test("using a Poster saves it as custom Poster and leaves the custom Backdrop untouched") {

        storedArtwork.value = artwork.copy(customBackdropPath = lowBackdrop.path)

        useImage(highPoster.path)

        storedArtwork.value?.customPosterPath shouldBe highPoster.path
        storedArtwork.value?.customBackdropPath shouldBe lowBackdrop.path
        buttonOf(highPoster.path) shouldBe ArtworkImageButton.CURRENT
        buttonOf(lowBackdrop.path) shouldBe ArtworkImageButton.CURRENT

    }

    test("on a Show, using a Poster saves it as custom Poster") {

        showGallery()

        useImage(highPoster.path)

        storedArtwork.value?.customPosterPath shouldBe highPoster.path
        buttonOf(highPoster.path) shouldBe ArtworkImageButton.CURRENT

    }

    test("a failed save leaves the state unchanged and emits no event") {

        coEvery { databaseRepository.setArtworkCustomBackdrop(any(), any()) } throws RuntimeException("boom")
        buttonOf(midBackdrop.path)
        val before = viewModel.uiState.value

        viewModel.event.test {
            viewModel.handleIntent(GalleryIntent.OnUseImageTap)
            testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
        }

        viewModel.uiState.value shouldBe before
        storedArtwork.value?.customBackdropPath shouldBe null

    }

    test("a tap on the current image saves nothing") {

        coEvery { apiRepository.getMovieImages(artwork.id) } returns listOf(tmdbPoster, highPoster)
        updateVm()
        buttonOf(tmdbPoster.path) shouldBe ArtworkImageButton.CURRENT

        viewModel.handleIntent(GalleryIntent.OnUseImageTap)
        testDispatcher.scheduler.advanceUntilIdle()

        storedArtwork.value shouldBe artwork

    }

    //endregion

})
