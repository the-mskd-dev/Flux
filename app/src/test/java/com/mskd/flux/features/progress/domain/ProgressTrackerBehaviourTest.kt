package com.mskd.flux.features.progress.domain

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.features.progress.ProgressHarness
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class ProgressTrackerBehaviourTest : FunSpec({

    fluxExtensions()

    lateinit var harness: ProgressHarness

    beforeTest { harness = ProgressHarness() }
    afterTest { harness.close() }

    context("recordProgress without chapters") {

        test("Movie below threshold becomes IS_WATCHING at position and enters History") {
            harness.seedMovie()
            val position = MediaMockups.movie.duration.minToMs.times(0.5).toLong()

            val result = harness.tracker.recordProgress(media = MediaMockups.movie, position = position)

            val expected = MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = position)
            result shouldBe expected
            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe expected
            harness.historyEntries().single().run {
                artworkId shouldBe MediaMockups.movieArtwork.id
                mediaId shouldBe MediaMockups.movie.mediaId
            }
        }

        test("Movie at threshold becomes WATCHED at zero and leaves History") {
            harness.seedMovie()
            val midPosition = MediaMockups.movie.duration.minToMs.times(0.5).toLong()
            val endPosition = MediaMockups.movie.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong()
            val watchingMovie = harness.tracker.recordProgress(media = MediaMockups.movie, position = midPosition)

            val result = harness.tracker.recordProgress(media = watchingMovie, position = endPosition)

            val expected = MediaMockups.movie.copy(status = Status.WATCHED, currentTime = 0L)
            result shouldBe expected
            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe expected
            harness.historyEntries().shouldBeEmpty()
        }

        test("non-last Episode below threshold becomes IS_WATCHING at position and enters History") {
            harness.seedShow()
            val position = MediaMockups.episode1.duration.minToMs.times(0.5).toLong()

            val result = harness.tracker.recordProgress(media = MediaMockups.episode1, position = position)

            val expected = MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = position)
            result shouldBe expected
            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episode1.id } shouldBe expected
            harness.historyEntries().single().run {
                artworkId shouldBe MediaMockups.showArtwork.id
                mediaId shouldBe MediaMockups.episode1.mediaId
            }
        }

        test("non-last Episode at threshold becomes WATCHED at zero and advances History to Next Episode") {
            harness.seedShow()
            val endPosition = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong()

            val result = harness.tracker.recordProgress(media = MediaMockups.episode1, position = endPosition)

            val expected = MediaMockups.episode1.copy(status = Status.WATCHED, currentTime = 0L)
            result shouldBe expected
            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episode1.id } shouldBe expected
            harness.historyEntries().single().run {
                artworkId shouldBe MediaMockups.showArtwork.id
                mediaId shouldBe MediaMockups.episode2.mediaId
            }
        }

        test("last Episode below threshold becomes IS_WATCHING at position and enters History") {
            harness.seedShow()
            val lastEpisode = MediaMockups.episodes.lastEpisode
            val position = lastEpisode.duration.minToMs.times(0.5).toLong()

            val result = harness.tracker.recordProgress(media = lastEpisode, position = position)

            val expected = lastEpisode.copy(status = Status.IS_WATCHING, currentTime = position)
            result shouldBe expected
            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == lastEpisode.id } shouldBe expected
            harness.historyEntries().single().run {
                artworkId shouldBe MediaMockups.showArtwork.id
                mediaId shouldBe lastEpisode.mediaId
            }
        }

        test("last Episode at threshold becomes WATCHED at zero and removes Show from History") {
            harness.seedShow()
            val lastEpisode = MediaMockups.episodes.lastEpisode
            val watchingLast = harness.tracker.recordProgress(media = lastEpisode, position = 100_000L)
            val endPosition = lastEpisode.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong()

            val result = harness.tracker.recordProgress(media = watchingLast, position = endPosition)

            val expected = lastEpisode.copy(status = Status.WATCHED, currentTime = 0L)
            result shouldBe expected
            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == lastEpisode.id } shouldBe expected
            harness.historyEntries().shouldBeEmpty()
        }

    }

    context("recordProgress with chapters and forceWatched") {

        val openingChapter = VideoChapter(index = 0, start = 0L, end = 90_000L, type = VideoChapterType.OPENING)
        val chapterOne = VideoChapter(index = 1, start = 90_000L, end = 1_200_000L, type = VideoChapterType.CHAPTER)
        val endingChapter = VideoChapter(index = 2, start = 1_200_000L, end = 1_300_000L, type = VideoChapterType.ENDING)
        val chapterTwo = VideoChapter(index = 3, start = 1_300_000L, end = 1_380_000L, type = VideoChapterType.CHAPTER)
        val finalEndingChapter = VideoChapter(index = 2, start = 1_200_000L, end = 1_380_000L, type = VideoChapterType.ENDING)

        test("position in last ENDING chapter marks WATCHED at zero and advances History") {
            harness.seedShow()
            harness.chapters = listOf(openingChapter, chapterOne, finalEndingChapter)

            val result = harness.tracker.recordProgress(media = MediaMockups.episode1, position = 1_210_000L)

            result shouldBe MediaMockups.episode1.copy(status = Status.WATCHED, currentTime = 0L)
            harness.historyEntries().single().mediaId shouldBe MediaMockups.episode2.mediaId
        }

        test("position in last non-ENDING chapter stays IS_WATCHING even above threshold") {
            harness.seedShow()
            harness.chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)

            val result = harness.tracker.recordProgress(media = MediaMockups.episode1, position = 1_350_000L)

            result shouldBe MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 1_350_000L)
            harness.historyEntries().single().mediaId shouldBe MediaMockups.episode1.mediaId
        }

        test("position in non-final ENDING chapter stays IS_WATCHING") {
            harness.seedShow()
            harness.chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)

            val result = harness.tracker.recordProgress(media = MediaMockups.episode1, position = 1_250_000L)

            result shouldBe MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 1_250_000L)
            harness.historyEntries().single().mediaId shouldBe MediaMockups.episode1.mediaId
        }

        test("position in OPENING chapter stays IS_WATCHING") {
            harness.seedShow()
            harness.chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)

            val result = harness.tracker.recordProgress(media = MediaMockups.episode1, position = 30_000L)

            result shouldBe MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 30_000L)
            harness.historyEntries().single().mediaId shouldBe MediaMockups.episode1.mediaId
        }

        test("position outside all chapters falls back to threshold") {
            harness.seedShow()
            val boundedChapters = listOf(
                VideoChapter(index = 0, start = 10_000L, end = 90_000L, type = VideoChapterType.OPENING),
                VideoChapter(index = 1, start = 90_000L, end = 1_000_000L, type = VideoChapterType.CHAPTER),
            )
            harness.chapters = boundedChapters

            val belowThreshold = harness.tracker.recordProgress(media = MediaMockups.episode1, position = 5_000L)
            belowThreshold shouldBe MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 5_000L)

            val aboveThresholdPosition = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong()
            val atThreshold = harness.tracker.recordProgress(media = belowThreshold, position = aboveThresholdPosition)
            atThreshold shouldBe MediaMockups.episode1.copy(status = Status.WATCHED, currentTime = 0L)
            harness.historyEntries().single().mediaId shouldBe MediaMockups.episode2.mediaId
        }

        test("forceWatched ignores chapters, skips reading them, and marks WATCHED at zero") {
            harness.seedShow()
            harness.onGetChapters = { error("Chapters must not be read when forceWatched = true") }

            val result = harness.tracker.recordProgress(
                media = MediaMockups.episode1,
                position = 1_350_000L,
                forceWatched = true,
            )

            result shouldBe MediaMockups.episode1.copy(status = Status.WATCHED, currentTime = 0L)
            harness.historyEntries().single().mediaId shouldBe MediaMockups.episode2.mediaId
        }

        test("forceWatched below threshold without chapters marks WATCHED at zero") {
            harness.seedShow()
            val midPosition = MediaMockups.episode1.duration.minToMs.times(0.5).toLong()

            val result = harness.tracker.recordProgress(
                media = MediaMockups.episode1,
                position = midPosition,
                forceWatched = true,
            )

            result shouldBe MediaMockups.episode1.copy(status = Status.WATCHED, currentTime = 0L)
            harness.historyEntries().single().mediaId shouldBe MediaMockups.episode2.mediaId
        }

        test("forceWatched = false keeps chapter rules") {
            harness.seedShow()
            harness.chapters = listOf(openingChapter, chapterOne, endingChapter, chapterTwo)

            val result = harness.tracker.recordProgress(
                media = MediaMockups.episode1,
                position = 1_350_000L,
                forceWatched = false,
            )

            result shouldBe MediaMockups.episode1.copy(status = Status.IS_WATCHING, currentTime = 1_350_000L)
        }

    }

    context("unified History rule") {

        test("only one History entry exists per Artwork") {
            harness.seedShow()

            harness.tracker.recordProgress(media = MediaMockups.episode1, position = 60_000L)
            harness.tracker.recordProgress(media = MediaMockups.episode2, position = 120_000L)

            val entries = harness.historyEntries()
            entries.shouldHaveSize(1)
            entries.single().run {
                artworkId shouldBe MediaMockups.showArtwork.id
                mediaId shouldBe MediaMockups.episode2.mediaId
            }
        }

        test("finishing the last Episode of season 1 inserts the first Episode of season 2 into History") {
            harness.seedShow()
            val endPosition = MediaMockups.episode2.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong()

            harness.tracker.recordProgress(media = MediaMockups.episode2, position = endPosition)

            harness.historyEntries().single().run {
                artworkId shouldBe MediaMockups.showArtwork.id
                mediaId shouldBe MediaMockups.episode3.mediaId
            }
        }

        test("finishing an Episode when Next Episode is on an Unavailable SAF source keeps it in History DB and hides it until the Source returns") {
            val safRoot = "content://com.android.externalstorage.documents/tree/primary%3ASeries"
            val safEpisode2 = MediaMockups.episode2.copy(
                file = UserFile(
                    name = MediaMockups.episode2.file.name,
                    path = "$safRoot/document/S01E02.mkv",
                    source = FileSource.SAF,
                )
            )
            harness.folders = listOf(UserFolder(path = safRoot, source = FileSource.SAF, isAvailable = false))
            harness.seedShow(episodes = listOf(MediaMockups.episode1, safEpisode2, MediaMockups.episode3))

            val endPosition = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong()
            harness.tracker.recordProgress(media = MediaMockups.episode1, position = endPosition)

            harness.historyEntries().single().mediaId shouldBe safEpisode2.mediaId
            harness.getHistory().first().shouldBeEmpty()

            harness.folders = listOf(UserFolder(path = safRoot, source = FileSource.SAF, isAvailable = true))
            harness.getHistory().first().single().media.mediaId shouldBe safEpisode2.mediaId
        }

        test("Private Movie never enters History") {
            harness.seedMovie(artwork = MediaMockups.movieArtwork.copy(isPrivate = true))
            val position = 120_000L

            harness.tracker.recordProgress(media = MediaMockups.movie, position = position)

            harness.database.getMovie(MediaMockups.movieArtwork.id)?.status shouldBe Status.IS_WATCHING
            harness.historyEntries().shouldBeEmpty()
        }

        test("Private Show never enters History when watching or finishing an Episode") {
            harness.seedShow(artwork = MediaMockups.showArtwork.copy(isPrivate = true))

            harness.tracker.recordProgress(media = MediaMockups.episode1, position = 60_000L)
            harness.historyEntries().shouldBeEmpty()

            harness.tracker.recordProgress(media = MediaMockups.episode1, position = 60_000L, forceWatched = true)
            harness.historyEntries().shouldBeEmpty()
        }

        test("Unknown Artwork Episode never enters History when watching or finishing") {
            harness.seedUnknown()

            harness.tracker.recordProgress(media = MediaMockups.unknownEpisode, position = 60_000L)
            harness.database.getUnknownMedias()
                .first { it.id == MediaMockups.unknownEpisode.id }
                .status shouldBe Status.IS_WATCHING
            harness.historyEntries().shouldBeEmpty()

            harness.tracker.recordProgress(media = MediaMockups.unknownEpisode, position = 60_000L, forceWatched = true)
            harness.database.getUnknownMedias()
                .first { it.id == MediaMockups.unknownEpisode.id }
                .status shouldBe Status.WATCHED
            harness.historyEntries().shouldBeEmpty()
        }

    }

    context("nextEpisode") {

        test("returns null when media is null") {
            harness.tracker.nextEpisode(media = null).shouldBeNull()
        }

        test("returns null when media is a Movie") {
            harness.seedMovie()
            harness.tracker.nextEpisode(media = MediaMockups.movie).shouldBeNull()
        }

        test("returns null when Episode belongs to the Unknown Artwork even with higher-numbered unknown Episodes") {
            val unknown1 = MediaMockups.unknownEpisode.copy(id = -101L, season = 1, number = 1)
            val unknown2 = Episode(
                file = UserFile(
                    name = "unknown episode S01E02.mkv",
                    addedDateTime = 0L,
                    path = "path/unknown_episode_S01E02.mkv",
                    source = FileSource.LOCAL,
                ),
                duration = 23,
            ).copy(id = -102L, season = 1, number = 2)
            harness.seedUnknown(episodes = listOf(unknown1, unknown2))

            harness.tracker.nextEpisode(media = unknown1).shouldBeNull()
        }

        test("returns next Episode in same season and across seasons") {
            harness.seedShow()

            harness.tracker.nextEpisode(media = MediaMockups.episode1) shouldBe MediaMockups.episode2
            harness.tracker.nextEpisode(media = MediaMockups.episode2) shouldBe MediaMockups.episode3
        }

        test("returns null for the last Episode") {
            harness.seedShow()

            harness.tracker.nextEpisode(media = MediaMockups.episode3).shouldBeNull()
        }

        test("onlyAvailable = true returns Next Episode on available SAF source") {
            val safRoot = "content://com.android.externalstorage.documents/tree/primary%3ASeries"
            val safEpisode2 = MediaMockups.episode2.copy(
                file = UserFile(
                    name = MediaMockups.episode2.file.name,
                    path = "$safRoot/document/S01E02.mkv",
                    source = FileSource.SAF,
                )
            )
            harness.folders = listOf(UserFolder(path = safRoot, source = FileSource.SAF, isAvailable = true))
            harness.seedShow(episodes = listOf(MediaMockups.episode1, safEpisode2))

            harness.tracker.nextEpisode(media = MediaMockups.episode1, onlyAvailable = true) shouldBe safEpisode2
        }

        test("onlyAvailable = true returns null and does not skip when immediate Next Episode is Unavailable") {
            val safRoot = "content://com.android.externalstorage.documents/tree/primary%3ASeries"
            val unavailableEpisode2 = MediaMockups.episode2.copy(
                file = UserFile(
                    name = MediaMockups.episode2.file.name,
                    path = "$safRoot/document/S01E02.mkv",
                    source = FileSource.SAF,
                )
            )
            harness.folders = listOf(UserFolder(path = safRoot, source = FileSource.SAF, isAvailable = false))
            harness.seedShow(episodes = listOf(MediaMockups.episode1, unavailableEpisode2, MediaMockups.episode3))

            harness.tracker.nextEpisode(media = MediaMockups.episode1, onlyAvailable = true).shouldBeNull()
        }

        test("onlyAvailable = true returns null when SAF folder is missing") {
            val safRoot = "content://com.android.externalstorage.documents/tree/primary%3AMissing"
            val missingSourceEpisode2 = MediaMockups.episode2.copy(
                file = UserFile(
                    name = MediaMockups.episode2.file.name,
                    path = "$safRoot/document/S01E02.mkv",
                    source = FileSource.SAF,
                )
            )
            harness.folders = emptyList()
            harness.seedShow(episodes = listOf(MediaMockups.episode1, missingSourceEpisode2))

            harness.tracker.nextEpisode(media = MediaMockups.episode1, onlyAvailable = true).shouldBeNull()
        }

        test("onlyAvailable = false returns an Unavailable SAF Next Episode") {
            val safRoot = "content://com.android.externalstorage.documents/tree/primary%3ASeries"
            val unavailableEpisode2 = MediaMockups.episode2.copy(
                file = UserFile(
                    name = MediaMockups.episode2.file.name,
                    path = "$safRoot/document/S01E02.mkv",
                    source = FileSource.SAF,
                )
            )
            harness.folders = listOf(UserFolder(path = safRoot, source = FileSource.SAF, isAvailable = false))
            harness.seedShow(episodes = listOf(MediaMockups.episode1, unavailableEpisode2))

            harness.tracker.nextEpisode(media = MediaMockups.episode1, onlyAvailable = false) shouldBe unavailableEpisode2
        }

    }

})
