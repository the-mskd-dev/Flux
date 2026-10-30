package com.mskd.flux.features.images.domain

import app.cash.turbine.test
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.core.ImagesPaths
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.utils.Constants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class ImagesPrefetchManagerTest : FunSpec({

    fluxExtensions()

    lateinit var database: DatabaseRepository
    lateinit var settings: SettingsDataStore
    lateinit var imageLoader: ImageLoader
    lateinit var imageRequestFactory: ImageRequestFactory
    lateinit var scope: CoroutineScope
    lateinit var prefetchManager: ImagesPrefetchManagerImpl

    val settingsFlow = MutableStateFlow(SettingsDataStore.State(prefetchHdImages = false))

    beforeTest {
        database = mockk(relaxed = true)
        settings = mockk(relaxed = true) {
            every { flow } returns settingsFlow
        }
        imageLoader = mockk(relaxed = true)
        imageRequestFactory = mockk(relaxed = true)
        scope = TestScope(UnconfinedTestDispatcher())

        prefetchManager = ImagesPrefetchManagerImpl(
            database = database,
            settings = settings,
            imageLoader = imageLoader,
            imageRequestFactory = imageRequestFactory,
            scope = scope
        )
    }

    test("prefetchImages does nothing when database has no images") {
        coEvery { database.getAllImagesPaths() } returns ImagesPaths()

        prefetchManager.state.test {
            awaitItem() shouldBe ImagesPrefetchManager.State.Idle
            prefetchManager.prefetchImages()
            expectNoEvents()
        }
    }

    test("prefetchImages enqueues requests and updates state to Idle when all complete") {
        val paths = listOf("path1", "path2")
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(images = paths)

        val callbacks = mutableListOf<(String) -> Unit>()
        every { imageRequestFactory.build(any(), any(), any()) } answers {
            val url = firstArg<String>()
            val callback = secondArg<(String) -> Unit>()
            callbacks.add(callback)
            mockk<ImageRequest>(relaxed = true)
        }

        prefetchManager.state.test {
            awaitItem() shouldBe ImagesPrefetchManager.State.Idle
            prefetchManager.prefetchImages()

            val inProgressState = awaitItem()
            inProgressState.shouldBeInstanceOf<ImagesPrefetchManager.State.InProgress>()

            // Complete first request
            callbacks[0](paths[0])
            // Completed 1/2 = 0.5 progress
            val secondState = awaitItem()
            secondState.shouldBeInstanceOf<ImagesPrefetchManager.State.InProgress>()
            (secondState as ImagesPrefetchManager.State.InProgress).progress shouldBe 0.5f

            // Complete second request
            callbacks[1](paths[1])
            awaitItem() shouldBe ImagesPrefetchManager.State.Idle
        }
    }

    test("prefetchImages requests HD images if prefetchHdImages is true") {
        settingsFlow.value = SettingsDataStore.State(prefetchHdImages = true)
        val paths = listOf("path1")
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(images = paths)

        val urls = mutableListOf<String>()
        every { imageRequestFactory.build(capture(urls), any(), any()) } returns mockk(relaxed = true)

        prefetchManager.prefetchImages()

        urls.size shouldBe 2 // both SD and HD urls
        urls[0] shouldBe "${Constants.TMDB.IMAGE}path1" // SD
        urls[1] shouldBe "${Constants.TMDB.IMAGE_LARGE}path1" // HD
    }

    test("prefetchImages requests the Logo in w500 at the display size") {
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(logos = listOf("/logo.png"))

        val urls = mutableListOf<String>()
        val sizes = mutableListOf<Size?>()
        every { imageRequestFactory.build(capture(urls), any(), captureNullable(sizes)) } returns mockk(relaxed = true)

        prefetchManager.prefetchImages()

        urls shouldBe listOf("https://image.tmdb.org/t/p/w500/logo.png")
        sizes shouldBe listOf(Size.ORIGINAL)
    }

    test("prefetchImages never requests the Logo in HD, even with prefetchHdImages") {
        settingsFlow.value = SettingsDataStore.State(prefetchHdImages = true)
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(logos = listOf("/logo.png"))

        val urls = mutableListOf<String>()
        every { imageRequestFactory.build(capture(urls), any(), any()) } returns mockk(relaxed = true)

        prefetchManager.prefetchImages()

        urls shouldBe listOf("https://image.tmdb.org/t/p/w500/logo.png")
    }

    test("prefetchImages requests nothing for null and empty Logo paths") {
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(logos = listOf(null, ""))

        prefetchManager.state.test {
            awaitItem() shouldBe ImagesPrefetchManager.State.Idle
            prefetchManager.prefetchImages()
            expectNoEvents()
        }

        verify(exactly = 0) { imageRequestFactory.build(any(), any(), any()) }
    }

    test("prefetchImages keeps the SD and HD requests of the other images next to the Logo") {
        settingsFlow.value = SettingsDataStore.State(prefetchHdImages = true)
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(images = listOf("/poster.jpg"), logos = listOf("/logo.png"))

        val urls = mutableListOf<String>()
        val sizes = mutableListOf<Size?>()
        // Each request ends at once, so the requests beyond the concurrency limit are built too
        every { imageRequestFactory.build(capture(urls), any(), captureNullable(sizes)) } answers {
            secondArg<(String) -> Unit>()(firstArg())
            mockk(relaxed = true)
        }

        prefetchManager.prefetchImages()

        urls.zip(sizes) shouldContainExactlyInAnyOrder listOf(
            "${Constants.TMDB.IMAGE}/poster.jpg" to null,
            "${Constants.TMDB.IMAGE_LARGE}/poster.jpg" to null,
            "https://image.tmdb.org/t/p/w500/logo.png" to Size.ORIGINAL,
        )
    }

})
