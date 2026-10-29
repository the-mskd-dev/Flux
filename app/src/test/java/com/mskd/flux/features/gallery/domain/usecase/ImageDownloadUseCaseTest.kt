package com.mskd.flux.features.gallery.domain.usecase

import com.mskd.flux.features.gallery.domain.datasource.CachedImageDataSource
import com.mskd.flux.features.gallery.domain.datasource.RemoteImageDataSource
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.domain.model.originalUrl
import com.mskd.flux.system.ImageSaver
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

private class FakeCache(private val entries: Map<String, ByteArray> = emptyMap()) : CachedImageDataSource {
    override suspend fun get(url: String): ByteArray? = entries[url]
}

private class FakeRemote(private val entries: Map<String, ByteArray> = emptyMap()) : RemoteImageDataSource {
    var calls = 0
    override suspend fun fetch(url: String): ByteArray {
        calls++
        return entries[url] ?: throw RuntimeException("no network")
    }
}

private class SavedImage(val bytes: ByteArray, val fileName: String, val mimeType: String)

private class FakeSaver(private val result: Result<Unit> = Result.success(Unit)) : ImageSaver {
    val saved = mutableListOf<SavedImage>()
    override suspend fun save(bytes: ByteArray, fileName: String, mimeType: String): Result<Unit> {
        saved += SavedImage(bytes, fileName, mimeType)
        return result
    }
}

class ImageDownloadUseCaseTest : FunSpec({

    val image = GalleryImage(path = "/abc123.jpg", type = GalleryImageType.BACKDROP, ratio = 1.78f, rating = 5.0)
    val cachedBytes = byteArrayOf(1, 2, 3)
    val networkBytes = byteArrayOf(4, 5, 6)

    test("cache hit saves the cached bytes without calling the network") {

        val remote = FakeRemote(mapOf(image.originalUrl to networkBytes))
        val saver = FakeSaver()
        val useCase = ImageDownloadUseCase(FakeCache(mapOf(image.originalUrl to cachedBytes)), remote, saver)

        useCase(image, "Inception")

        saver.saved.single().bytes shouldBe cachedBytes
        remote.calls shouldBe 0

    }

    test("cache miss saves the network bytes") {

        val saver = FakeSaver()
        val useCase = ImageDownloadUseCase(FakeCache(), FakeRemote(mapOf(image.originalUrl to networkBytes)), saver)

        useCase(image, "Inception")

        saver.saved.single().bytes shouldBe networkBytes

    }

    test("neither source fails and nothing is saved") {

        val saver = FakeSaver()
        val useCase = ImageDownloadUseCase(FakeCache(), FakeRemote(), saver)

        shouldThrow<RuntimeException> { useCase(image, "Inception") }

        saver.saved.size shouldBe 0

    }

    test("saver failure is propagated") {

        val useCase = ImageDownloadUseCase(
            cache = FakeCache(mapOf(image.originalUrl to cachedBytes)),
            remote = FakeRemote(),
            saver = FakeSaver(Result.failure(IllegalStateException("disk full"))),
        )

        shouldThrow<IllegalStateException> { useCase(image, "Inception") }

    }

    fun fileNameOf(title: String, path: String): Pair<String, String> {
        val image = image.copy(path = path)
        val saver = FakeSaver()
        runBlocking {
            ImageDownloadUseCase(FakeCache(mapOf(image.originalUrl to cachedBytes)), FakeRemote(), saver)(image, title)
        }
        return saver.saved.single().let { it.fileName to it.mimeType }
    }

    test("file name is the title and the basename of the path, with the extension of the path") {

        fileNameOf("Inception", "/abc123.jpg") shouldBe ("Inception_abc123.jpg" to "image/jpeg")
        fileNameOf("Inception", "/abc123.png") shouldBe ("Inception_abc123.png" to "image/png")
        fileNameOf("Inception", "/abc123.webp") shouldBe ("Inception_abc123.webp" to "image/webp")

    }

    test("forbidden characters are cleaned from the file name") {

        fileNameOf("Face/Off", "/abc123.jpg").first shouldBe "Face_Off_abc123.jpg"
        fileNameOf("What? <Is> \"This\": A|B*C\\D", "/abc123.jpg").first shouldBe "What_ _Is_ _This__ A_B_C_D_abc123.jpg"

    }

    test("a very long title is shortened") {

        fileNameOf("A".repeat(300), "/abc123.jpg").first shouldBe "A".repeat(60) + "_abc123.jpg"

    }

    test("a blank title leaves only the basename") {

        fileNameOf("  ", "/abc123.jpg").first shouldBe "abc123.jpg"

    }

})
