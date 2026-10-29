package com.mskd.flux.features.progress.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.domain.usecase.SaveToHistoryUseCase
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.features.player.domain.usecase.GetChaptersUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.progress.fake.ProgressUCTestCases
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk

class SaveProgressUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var databaseRepository: DatabaseRepository
    lateinit var saveProgress: SaveProgressUseCase
    lateinit var saveToHistory: SaveToHistoryUseCase

    /** Builds a [SaveProgressUseCase] whose [GetChaptersUseCase] returns the given [chapters]. */
    fun buildSaveProgress(chapters: List<VideoChapter> = emptyList()): SaveProgressUseCase {
        val getChaptersUseCase = mockk<GetChaptersUseCase>(relaxed = true)
        coEvery { getChaptersUseCase(any()) } returns chapters
        return SaveProgressUseCase(
            database = databaseRepository,
            saveToHistory = saveToHistory,
            getChaptersUseCase = getChaptersUseCase
        )
    }

    beforeTest {

        databaseRepository = spyk(FakeDatabaseRepository())
        saveToHistory = mockk(relaxed = true)

        saveProgress = buildSaveProgress()

    }

    context("save progress") {
        withData(
            nameFn = { it.description },
            ProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the middle",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(0.5).toLong(),
                statusExpected = Status.IS_WATCHING
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the end",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                statusExpected = Status.WATCHED
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(0.5).toLong(),
                statusExpected = Status.IS_WATCHING
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                statusExpected = Status.WATCHED
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time for last episode at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                progress = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.5).toLong(),
                statusExpected = Status.IS_WATCHING
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time for last episode at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                progress = MediaMockups.episodes.lastEpisode.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                statusExpected = Status.WATCHED
            )
        ) { testCase ->

            // Given
            val expectedProgress = if (testCase.statusExpected == Status.WATCHED) 0L else testCase.progress
            val expectedMedia = when (val media = testCase.media) {
                is Movie -> media.copy(currentTime = expectedProgress, status = testCase.statusExpected)
                is Episode -> media.copy(currentTime = expectedProgress, status = testCase.statusExpected)
            }

            // When
            saveProgress(media = testCase.media, progress = testCase.progress)

            // Then
            coVerify { databaseRepository.saveMedias(listOf(expectedMedia)) }
            coVerify { saveToHistory(expectedMedia) }

        }

    }

    context("save progress with chapters") {

        val openingChapter = VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING)
        val chapterOne = VideoChapter(index = 1, start = 90_000L, end = 1_200_000L, type = VideoChapterType.CHAPTER)
        val endingChapter = VideoChapter(index = 2, start = 1_200_000L, end = 1_300_000L, type = VideoChapterType.ENDING)
        val chapterTwo = VideoChapter(index = 3, start = 1_300_000L, end = 1_380_000L, type = VideoChapterType.CHAPTER)
        val finalEndingChapter = VideoChapter(index = 2, start = 1_200_000L, end = 1_380_000L, type = VideoChapterType.ENDING)

        test("status is WATCHED when progress is in a last ENDING chapter") {

            // Given
            saveProgress = buildSaveProgress(
                chapters = listOf(openingChapter, chapterOne, finalEndingChapter)
            )

            // When
            saveProgress(media = MediaMockups.episode1, progress = 1_300_000L)

            // Then
            coVerify {
                databaseRepository.saveMedias(listOf(MediaMockups.episode1.copy(currentTime = 0L, status = Status.WATCHED)))
            }

        }

        test("status is IS_WATCHING when progress is in a non-final ENDING chapter") {

            // Given
            saveProgress = buildSaveProgress(
                chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)
            )

            // When : progress is in the ending chapter, but the last chapter is a normal one
            saveProgress(media = MediaMockups.episode1, progress = 1_250_000L)

            // Then
            coVerify {
                databaseRepository.saveMedias(listOf(MediaMockups.episode1.copy(currentTime = 1_250_000L, status = Status.IS_WATCHING)))
            }

        }

        test("status is IS_WATCHING when progress is in the OPENING chapter") {

            // Given
            saveProgress = buildSaveProgress(
                chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)
            )

            // When
            saveProgress(media = MediaMockups.episode1, progress = 30_000L)

            // Then
            coVerify {
                databaseRepository.saveMedias(listOf(MediaMockups.episode1.copy(currentTime = 30_000L, status = Status.IS_WATCHING)))
            }

        }

        test("forced WATCHED saves the media WATCHED even when the last chapter is not an ENDING") {

            // Given
            saveProgress = buildSaveProgress(
                chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)
            )

            // When : progress is in the last chapter, which is not an ending
            saveProgress(media = MediaMockups.episode1, progress = 1_350_000L, forceWatched = true)

            // Then
            val expectedMedia = MediaMockups.episode1.copy(currentTime = 0L, status = Status.WATCHED)
            coVerify { databaseRepository.saveMedias(listOf(expectedMedia)) }
            coVerify { saveToHistory(expectedMedia) }

        }

        test("forced WATCHED saves the media WATCHED without chapters below the threshold") {

            // Given
            saveProgress = buildSaveProgress(chapters = emptyList())

            // When
            saveProgress(
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(0.5).toLong(),
                forceWatched = true
            )

            // Then
            val expectedMedia = MediaMockups.episode1.copy(currentTime = 0L, status = Status.WATCHED)
            coVerify { databaseRepository.saveMedias(listOf(expectedMedia)) }
            coVerify { saveToHistory(expectedMedia) }

        }

        test("not forced keeps the chapter rules") {

            // Given
            saveProgress = buildSaveProgress(
                chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)
            )

            // When
            saveProgress(media = MediaMockups.episode1, progress = 1_350_000L, forceWatched = false)

            // Then
            coVerify {
                databaseRepository.saveMedias(listOf(MediaMockups.episode1.copy(currentTime = 1_350_000L, status = Status.IS_WATCHING)))
            }

        }

        test("status falls back to the threshold when there are no chapters") {

            // Given
            saveProgress = buildSaveProgress(chapters = emptyList())

            // When
            saveProgress(
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong()
            )

            // Then
            coVerify {
                databaseRepository.saveMedias(listOf(MediaMockups.episode1.copy(currentTime = 0L, status = Status.WATCHED)))
            }

        }

    }

})
