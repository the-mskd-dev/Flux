package com.mskd.flux.features.player.data.usecase

import android.content.Context
import android.net.Uri
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.mockups.MediaMockups
import io.github.anilbeesetti.nextlib.mediainfo.Chapter
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfo
import io.github.anilbeesetti.nextlib.mediainfo.MediaInfoBuilder
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll

class AndroidGetChaptersUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var context: Context
    lateinit var builder: MediaInfoBuilder
    lateinit var useCase: AndroidGetChaptersUseCase

    beforeTest {

        context = mockk(relaxed = true)

        // The use case converts the file path to an android.net.Uri
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        // Injected so the real builder (which loads a native library) is never constructed on the JVM
        builder = mockk(relaxed = true)
        useCase = AndroidGetChaptersUseCase(
            context = context,
            createMediaInfoBuilder = { builder }
        )

    }

    afterTest {
        unmockkAll()
    }

    /** Stubs the injected [builder] so `from().build()` returns [mediaInfo]. */
    fun everyBuiltMediaInfoIs(mediaInfo: MediaInfo?) {
        every { builder.from(any(), any()) } returns builder
        every { builder.build() } returns mediaInfo
    }

    test("invoke returns an empty list when the media info is not available") {

        // Given
        everyBuiltMediaInfoIs(mediaInfo = null)

        // When
        val chapters = useCase(media = MediaMockups.episode1)

        // Then
        chapters.shouldBeEmpty()

    }

    test("invoke returns an empty list when reading the media info fails") {

        // Given
        every { builder.from(any(), any()) } throws RuntimeException("Failed to read media info")

        // When
        val chapters = useCase(media = MediaMockups.episode1)

        // Then
        chapters.shouldBeEmpty()

    }

    test("invoke maps the media info chapters with their types") {

        // Given
        val mediaInfo = MediaInfo(
            format = "Matroska",
            duration = 1_380_000L,
            videoStream = null,
            audioStreams = emptyList(),
            subtitleStreams = emptyList(),
            chapters = listOf(
                Chapter(index = 0, start = 0L, end = 90_000L, title = "Opening"),
                Chapter(index = 1, start = 90_000L, end = 1_200_000L, title = "Episode 1"),
                Chapter(index = 2, start = 1_200_000L, end = 1_380_000L, title = "Ending"),
                Chapter(index = 3, start = 1_380_000L, end = 1_400_000L, title = null),
            ),
            frameLoaderContext = null
        )
        everyBuiltMediaInfoIs(mediaInfo)

        // When
        val chapters = useCase(media = MediaMockups.episode1)

        // Then
        chapters shouldBe listOf(
            VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING),
            VideoChapter(index = 1, start = 90_000L, end = 1_200_000L, type = VideoChapterType.CHAPTER),
            VideoChapter(index = 2, start = 1_200_000L, end = 1_380_000L, type = VideoChapterType.ENDING),
            VideoChapter(index = 3, start = 1_380_000L, end = 1_400_000L, type = VideoChapterType.CHAPTER),
        )

    }

    context("VideoChapterType.fromString") {
        withData(
            nameFn = { (title, _) -> "[$title]" },
            "op" to VideoChapterType.OPENING,
            "Opening" to VideoChapterType.OPENING,
            "op 2" to VideoChapterType.OPENING,
            "NCOP" to VideoChapterType.OPENING,
            "Intro" to VideoChapterType.OPENING,
            "Introduction" to VideoChapterType.OPENING,
            "ed" to VideoChapterType.ENDING,
            "Ending" to VideoChapterType.ENDING,
            "NCED" to VideoChapterType.ENDING,
            "Outro" to VideoChapterType.ENDING,
            "Credits" to VideoChapterType.ENDING,
            "Credit" to VideoChapterType.ENDING,
            "End Credits" to VideoChapterType.ENDING,
            "end" to VideoChapterType.ENDING,
            "Episode 12" to VideoChapterType.CHAPTER,
            "Random title" to VideoChapterType.CHAPTER,
            "" to VideoChapterType.CHAPTER,
            "   " to VideoChapterType.CHAPTER,
            null to VideoChapterType.CHAPTER
        ) { (title, expectedType) ->

            VideoChapterType.fromString(title) shouldBe expectedType

        }
    }

})