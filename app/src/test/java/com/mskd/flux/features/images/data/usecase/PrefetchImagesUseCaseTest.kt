package com.mskd.flux.features.images.data.usecase

import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.size.Size
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.core.ImagesPaths
import com.mskd.flux.features.images.domain.ImageRequestFactory
import com.mskd.flux.utils.Constants
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class PrefetchImagesUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var database: DatabaseRepository
    lateinit var imageLoader: ImageLoader
    lateinit var imageRequestFactory: ImageRequestFactory
    lateinit var scope: CoroutineScope
    lateinit var prefetchImagesUseCase: PrefetchImagesUseCaseImpl

    beforeTest {
        database = mockk(relaxed = true)
        imageLoader = mockk(relaxed = true)
        imageRequestFactory = mockk(relaxed = true)
        scope = TestScope(UnconfinedTestDispatcher())

        prefetchImagesUseCase = PrefetchImagesUseCaseImpl(
            database = database,
            imageLoader = imageLoader,
            imageRequestFactory = imageRequestFactory,
            scope = scope
        )
    }

    test("prefetchImages does nothing when database has no images") {
        coEvery { database.getAllImagesPaths() } returns ImagesPaths()

        prefetchImagesUseCase()

        verify(exactly = 0) { imageRequestFactory.build(any(), any(), any()) }
        verify(exactly = 0) { imageLoader.enqueue(any()) }
    }

    test("prefetchImages always requests both SD and HD images") {
        val paths = listOf("path1")
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(images = paths)

        val urls = mutableListOf<String>()
        val sizes = mutableListOf<Size?>()
        every { imageRequestFactory.build(capture(urls), any(), captureNullable(sizes)) } returns mockk(relaxed = true)

        prefetchImagesUseCase()

        urls.zip(sizes) shouldContainExactlyInAnyOrder listOf(
            "${Constants.TMDB.IMAGE}path1" to null,
            "${Constants.TMDB.IMAGE_LARGE}path1" to null,
        )
        verify(exactly = 2) { imageLoader.enqueue(any()) }
    }

    test("prefetchImages skips URLs already pending until they complete") {
        val paths = listOf("path1")
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(images = paths)

        val callbacks = mutableListOf<Pair<String, (String) -> Unit>>()
        every { imageRequestFactory.build(any(), any(), any()) } answers {
            callbacks.add(firstArg<String>() to secondArg())
            mockk<ImageRequest>(relaxed = true)
        }

        prefetchImagesUseCase()
        callbacks.size shouldBe 2

        prefetchImagesUseCase()
        callbacks.size shouldBe 2

        callbacks.toList().forEach { (url, onEnd) -> onEnd(url) }

        prefetchImagesUseCase()
        callbacks.size shouldBe 4
        verify(exactly = 4) { imageLoader.enqueue(any()) }
    }

    test("prefetchImages requests the Logo in w500 at the display size and never in HD") {
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(logos = listOf("/logo.png"))

        val urls = mutableListOf<String>()
        val sizes = mutableListOf<Size?>()
        every { imageRequestFactory.build(capture(urls), any(), captureNullable(sizes)) } returns mockk(relaxed = true)

        prefetchImagesUseCase()

        urls shouldBe listOf("https://image.tmdb.org/t/p/w500/logo.png")
        sizes shouldBe listOf(Size.ORIGINAL)
    }

    test("prefetchImages requests nothing for null and empty Logo paths") {
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(logos = listOf(null, ""))

        prefetchImagesUseCase()

        verify(exactly = 0) { imageRequestFactory.build(any(), any(), any()) }
        verify(exactly = 0) { imageLoader.enqueue(any()) }
    }

    test("prefetchImages keeps the SD and HD requests of the other images next to the Logo") {
        coEvery { database.getAllImagesPaths() } returns ImagesPaths(images = listOf("/poster.jpg"), logos = listOf("/logo.png"))

        val urls = mutableListOf<String>()
        val sizes = mutableListOf<Size?>()
        // Each request ends at once, so the requests beyond the concurrency limit are built too
        every { imageRequestFactory.build(capture(urls), any(), captureNullable(sizes)) } answers {
            secondArg<(String) -> Unit>()(firstArg())
            mockk(relaxed = true)
        }

        prefetchImagesUseCase()

        urls.zip(sizes) shouldContainExactlyInAnyOrder listOf(
            "${Constants.TMDB.IMAGE}/poster.jpg" to null,
            "${Constants.TMDB.IMAGE_LARGE}/poster.jpg" to null,
            "https://image.tmdb.org/t/p/w500/logo.png" to Size.ORIGINAL,
        )
    }

})
