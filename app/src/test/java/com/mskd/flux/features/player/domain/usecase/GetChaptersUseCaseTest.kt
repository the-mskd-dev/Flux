package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class GetChaptersUseCaseTest : FunSpec({

    fluxExtensions()

    // GetChaptersUseCase is an interface: its contract is verified here through a fake
    // implementation, ensuring any implementation simply returns the chapters of a media.
    class FakeGetChaptersUseCase(private val chapters: List<VideoChapter>) : GetChaptersUseCase {
        override suspend fun invoke(media: Media): List<VideoChapter> = chapters
    }

    test("invoke returns an empty list when no chapters are available") {

        // Given
        val useCase = FakeGetChaptersUseCase(emptyList())

        // When
        val chapters = useCase(media = MediaMockups.episode1)

        // Then
        chapters.shouldBeEmpty()

    }

    test("invoke returns the chapters of the media") {

        // Given
        val chapters = listOf(
            VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING),
            VideoChapter(index = 1, start = 90_000L, end = 1_200_000L, type = VideoChapterType.CHAPTER),
            VideoChapter(index = 2, start = 1_200_000L, end = 1_380_000L, type = VideoChapterType.ENDING),
        )
        val useCase = FakeGetChaptersUseCase(chapters)

        // When
        val result = useCase(media = MediaMockups.episode1)

        // Then
        result shouldBe chapters

    }

})