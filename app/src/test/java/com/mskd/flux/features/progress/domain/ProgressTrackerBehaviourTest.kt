package com.mskd.flux.features.progress.domain

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
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

    context("setArtworkPrivacy") {

        test("marking an Artwork private updates isPrivate, preserves Status and Progress, and removes only its History entry") {
            harness.seedMovie()
            harness.seedMovie(artwork = MediaMockups.movieArtwork2, movie = MediaMockups.movie2)
            val position1 = 120_000L
            val position2 = 180_000L
            harness.tracker.recordProgress(media = MediaMockups.movie, position = position1)
            harness.tracker.recordProgress(media = MediaMockups.movie2, position = position2)

            harness.tracker.setArtworkPrivacy(artworkId = MediaMockups.movieArtwork.id, isPrivate = true)

            harness.database.getArtwork(MediaMockups.movieArtwork.id)?.isPrivate shouldBe true
            harness.database.getArtwork(MediaMockups.movieArtwork2.id)?.isPrivate shouldBe false
            harness.historyEntries().map { it.artworkId } shouldBe listOf(MediaMockups.movieArtwork2.id)
            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = position1)
        }

        test("marking a Private Artwork public updates isPrivate, preserves Status and Progress, and leaves History untouched") {
            harness.seedMovie(artwork = MediaMockups.movieArtwork.copy(isPrivate = true), movie = MediaMockups.movie)
            harness.seedMovie(artwork = MediaMockups.movieArtwork2, movie = MediaMockups.movie2)
            val position1 = 120_000L
            val position2 = 180_000L
            harness.tracker.recordProgress(media = MediaMockups.movie, position = position1)
            harness.tracker.recordProgress(media = MediaMockups.movie2, position = position2)

            harness.tracker.setArtworkPrivacy(artworkId = MediaMockups.movieArtwork.id, isPrivate = false)

            harness.database.getArtwork(MediaMockups.movieArtwork.id)?.isPrivate shouldBe false
            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = position1)
            harness.historyEntries().map { it.artworkId } shouldBe listOf(MediaMockups.movieArtwork2.id)
        }

        test("setting isPrivate = false on an Artwork already in History leaves its History entry untouched") {
            harness.seedMovie()
            harness.tracker.recordProgress(media = MediaMockups.movie, position = 120_000L)

            harness.tracker.setArtworkPrivacy(artworkId = MediaMockups.movieArtwork.id, isPrivate = false)

            harness.database.getArtwork(MediaMockups.movieArtwork.id)?.isPrivate shouldBe false
            harness.historyEntries().single().artworkId shouldBe MediaMockups.movieArtwork.id
        }

    }

    context("markNsfwArtworksPrivate") {

        test("marks every NSFW Artwork private and removes their History entries while leaving non-NSFW Artworks untouched") {
            harness.seedMovie(artwork = MediaMockups.movieArtwork.copy(nsfw = true), movie = MediaMockups.movie)
            harness.seedShow(artwork = MediaMockups.showArtwork.copy(nsfw = true), episodes = MediaMockups.episodes)
            harness.seedMovie(artwork = MediaMockups.movieArtwork2.copy(nsfw = false), movie = MediaMockups.movie2)
            harness.tracker.recordProgress(media = MediaMockups.movie, position = 120_000L)
            harness.tracker.recordProgress(media = MediaMockups.episode1, position = 60_000L)
            harness.tracker.recordProgress(media = MediaMockups.movie2, position = 180_000L)

            harness.tracker.markNsfwArtworksPrivate()

            harness.database.getArtwork(MediaMockups.movieArtwork.id)?.isPrivate shouldBe true
            harness.database.getArtwork(MediaMockups.showArtwork.id)?.isPrivate shouldBe true
            harness.database.getArtwork(MediaMockups.movieArtwork2.id)?.isPrivate shouldBe false
            harness.historyEntries().map { it.artworkId } shouldBe listOf(MediaMockups.movieArtwork2.id)
        }

        test("leaves non-NSFW Artworks and History untouched when no NSFW Artworks exist") {
            harness.seedMovie(artwork = MediaMockups.movieArtwork.copy(nsfw = false), movie = MediaMockups.movie)
            harness.tracker.recordProgress(media = MediaMockups.movie, position = 120_000L)

            harness.tracker.markNsfwArtworksPrivate()

            harness.database.getArtwork(MediaMockups.movieArtwork.id)?.isPrivate shouldBe false
            harness.historyEntries().single().artworkId shouldBe MediaMockups.movieArtwork.id
        }

    }

    context("removeFromHistory") {

        test("deletes the targeted Artwork History entry without altering Media Status or Progress and leaves other entries intact") {
            harness.seedMovie()
            harness.seedShow()
            harness.tracker.recordProgress(media = MediaMockups.movie, position = 120_000L)
            harness.tracker.recordProgress(media = MediaMockups.episode1, position = 60_000L)

            harness.tracker.removeFromHistory(artworkId = MediaMockups.movieArtwork.id)

            harness.historyEntries().map { it.artworkId } shouldBe listOf(MediaMockups.showArtwork.id)
            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = 120_000L)
        }

        test("does nothing when the Artwork has no History entry") {
            harness.seedMovie()
            harness.seedShow()
            harness.tracker.recordProgress(media = MediaMockups.episode1, position = 60_000L)

            harness.tracker.removeFromHistory(artworkId = MediaMockups.movieArtwork.id)

            harness.historyEntries().single().artworkId shouldBe MediaMockups.showArtwork.id
            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe MediaMockups.movie
        }

    }

    context("setWatched") {

        test("watched = true on Movie resets currentTime to zero and deletes History") {
            val watchingMovie = MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = 120_000L)
            harness.seedMovie(movie = watchingMovie)
            harness.history.insert(media = watchingMovie)

            harness.tracker.setWatched(media = watchingMovie, watched = true)

            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.WATCHED, currentTime = 0L)
            harness.historyEntries().shouldBeEmpty()
        }

        test("watched = false on Movie resets currentTime to zero and leaves History untouched") {
            val watchingMovie = MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = 120_000L)
            harness.seedMovie(movie = watchingMovie)
            harness.history.insert(media = watchingMovie)
            val historyBefore = harness.historyEntries()

            harness.tracker.setWatched(media = watchingMovie, watched = false)

            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.TO_WATCH, currentTime = 0L)
            harness.historyEntries() shouldBe historyBefore
        }

        test("watched = true on non-last Episode resets currentTime to zero and advances History to Next Episode") {
            val watchingEpisode2 = MediaMockups.episode2.copy(status = Status.IS_WATCHING, currentTime = 60_000L)
            harness.seedShow(episodes = listOf(MediaMockups.episode1, watchingEpisode2, MediaMockups.episode3))
            harness.history.insert(media = watchingEpisode2)

            harness.tracker.setWatched(media = watchingEpisode2, watched = true)

            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episode2.id } shouldBe
                MediaMockups.episode2.copy(status = Status.WATCHED, currentTime = 0L)
            harness.historyEntries().single().run {
                artworkId shouldBe MediaMockups.showArtwork.id
                mediaId shouldBe MediaMockups.episode3.mediaId
            }
        }

        test("watched = false on non-last Episode resets currentTime to zero and leaves History untouched") {
            val watchedEpisode1 = MediaMockups.episode1.copy(status = Status.WATCHED, currentTime = 0L)
            val watchingEpisode2 = MediaMockups.episode2.copy(status = Status.IS_WATCHING, currentTime = 90_000L)
            harness.seedShow(episodes = listOf(watchedEpisode1, watchingEpisode2, MediaMockups.episode3))
            harness.history.insert(media = watchingEpisode2)
            val historyBefore = harness.historyEntries()

            harness.tracker.setWatched(media = watchedEpisode1, watched = false)

            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episode1.id } shouldBe
                MediaMockups.episode1.copy(status = Status.TO_WATCH, currentTime = 0L)
            harness.historyEntries() shouldBe historyBefore
        }

        test("watched = true on last Episode resets currentTime to zero and removes Show from History") {
            val watchingLast = MediaMockups.episodes.lastEpisode.copy(status = Status.IS_WATCHING, currentTime = 60_000L)
            harness.seedShow(episodes = listOf(MediaMockups.episode1, MediaMockups.episode2, watchingLast))
            harness.history.insert(media = watchingLast)

            harness.tracker.setWatched(media = watchingLast, watched = true)

            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == watchingLast.id } shouldBe
                watchingLast.copy(status = Status.WATCHED, currentTime = 0L)
            harness.historyEntries().shouldBeEmpty()
        }

        test("watched = false on last Episode resets currentTime to zero and leaves History untouched") {
            val watchingLast = MediaMockups.episodes.lastEpisode.copy(status = Status.IS_WATCHING, currentTime = 60_000L)
            harness.seedShow(episodes = listOf(MediaMockups.episode1, MediaMockups.episode2, watchingLast))
            harness.history.insert(media = watchingLast)
            val historyBefore = harness.historyEntries()

            harness.tracker.setWatched(media = watchingLast, watched = false)

            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == watchingLast.id } shouldBe
                watchingLast.copy(status = Status.TO_WATCH, currentTime = 0L)
            harness.historyEntries() shouldBe historyBefore
        }

        test("Private Movie never enters History via setWatched") {
            harness.seedMovie(artwork = MediaMockups.movieArtwork.copy(isPrivate = true))

            harness.tracker.setWatched(media = MediaMockups.movie, watched = true)
            harness.database.getMovie(MediaMockups.movieArtwork.id)?.status shouldBe Status.WATCHED
            harness.historyEntries().shouldBeEmpty()

            harness.tracker.setWatched(media = MediaMockups.movie.copy(status = Status.WATCHED), watched = false)
            harness.database.getMovie(MediaMockups.movieArtwork.id)?.status shouldBe Status.TO_WATCH
            harness.historyEntries().shouldBeEmpty()
        }

        test("Private Show never enters History via setWatched") {
            harness.seedShow(artwork = MediaMockups.showArtwork.copy(isPrivate = true))

            harness.tracker.setWatched(media = MediaMockups.episode1, watched = true)
            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episode1.id }.status shouldBe Status.WATCHED
            harness.historyEntries().shouldBeEmpty()

            harness.tracker.setWatched(media = MediaMockups.episode3, watched = true)
            harness.historyEntries().shouldBeEmpty()

            harness.tracker.setWatched(media = MediaMockups.episode1.copy(status = Status.WATCHED), watched = false)
            harness.historyEntries().shouldBeEmpty()
        }

        test("Unknown Artwork Episode never enters History via setWatched") {
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

            harness.tracker.setWatched(media = unknown1, watched = true)
            harness.database.getUnknownMedias().first { it.id == unknown1.id }.status shouldBe Status.WATCHED
            harness.historyEntries().shouldBeEmpty()

            harness.tracker.setWatched(media = unknown1.copy(status = Status.WATCHED), watched = false)
            harness.database.getUnknownMedias().first { it.id == unknown1.id }.status shouldBe Status.TO_WATCH
            harness.historyEntries().shouldBeEmpty()
        }

        test("setWatched(watched = true) agrees with recordProgress(forceWatched = true) across all media variants") {
            suspend fun assertAgreement(
                seed: suspend ProgressHarness.() -> Media,
                readMedia: suspend ProgressHarness.() -> Media?,
            ) {
                ProgressHarness().use { manualHarness ->
                    ProgressHarness().use { playbackHarness ->
                        val manualTarget = manualHarness.seed()
                        val playbackTarget = playbackHarness.seed()

                        manualHarness.tracker.setWatched(media = manualTarget, watched = true)
                        playbackHarness.tracker.recordProgress(
                            media = playbackTarget,
                            position = 60_000L,
                            forceWatched = true,
                        )

                        manualHarness.readMedia() shouldBe playbackHarness.readMedia()
                        manualHarness.historyEntries().map { it.artworkId to it.mediaId } shouldBe
                            playbackHarness.historyEntries().map { it.artworkId to it.mediaId }
                    }
                }
            }

            // Public Movie (with existing History entry)
            assertAgreement(
                seed = {
                    val watching = MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = 50_000L)
                    seedMovie(movie = watching)
                    history.insert(media = watching)
                    watching
                },
                readMedia = { database.getMovie(MediaMockups.movieArtwork.id) },
            )

            // Public non-last Episode
            assertAgreement(
                seed = {
                    seedShow()
                    history.insert(media = MediaMockups.episode1)
                    MediaMockups.episode1
                },
                readMedia = { database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episode1.id } },
            )

            // Public last Episode (with existing History entry)
            assertAgreement(
                seed = {
                    val last = MediaMockups.episodes.lastEpisode.copy(status = Status.IS_WATCHING, currentTime = 40_000L)
                    seedShow(episodes = listOf(MediaMockups.episode1, MediaMockups.episode2, last))
                    history.insert(media = last)
                    last
                },
                readMedia = { database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episodes.lastEpisode.id } },
            )

            // Private Movie
            assertAgreement(
                seed = {
                    seedMovie(artwork = MediaMockups.movieArtwork.copy(isPrivate = true))
                    MediaMockups.movie
                },
                readMedia = { database.getMovie(MediaMockups.movieArtwork.id) },
            )

            // Private Show Episode
            assertAgreement(
                seed = {
                    seedShow(artwork = MediaMockups.showArtwork.copy(isPrivate = true))
                    MediaMockups.episode1
                },
                readMedia = { database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == MediaMockups.episode1.id } },
            )

            // Unknown Artwork Episode
            assertAgreement(
                seed = {
                    seedUnknown()
                    MediaMockups.unknownEpisode
                },
                readMedia = { database.getUnknownMedias().first { it.id == MediaMockups.unknownEpisode.id } },
            )
        }

    }

    context("markPreviousAsWatched") {

        test("marks earlier unwatched Episodes WATCHED at zero and leaves later and target Episodes untouched") {
            val episode4 = MediaMockups.episode3.copy(
                id = 761475L,
                number = 34,
                file = UserFile(
                    name = "naruto_shippuuden_S02E34.mkv",
                    addedDateTime = 0L,
                    path = "path/naruto_shippuuden_S02E34.mkv",
                    source = FileSource.LOCAL,
                ),
            )
            val ep1 = MediaMockups.episode1.copy(status = Status.TO_WATCH, currentTime = 0L)
            val ep2 = MediaMockups.episode2.copy(status = Status.IS_WATCHING, currentTime = 120_000L)
            val ep3 = MediaMockups.episode3.copy(status = Status.WATCHED, currentTime = 0L)
            harness.seedShow(episodes = listOf(ep1, ep2, ep3, episode4))

            harness.tracker.markPreviousAsWatched(episode = ep3)

            val saved = harness.database.getEpisodes(MediaMockups.showArtwork.id).associateBy { it.id }
            saved[ep1.id] shouldBe ep1.copy(status = Status.WATCHED, currentTime = 0L)
            saved[ep2.id] shouldBe ep2.copy(status = Status.WATCHED, currentTime = 0L)
            saved[ep3.id] shouldBe ep3
            saved[episode4.id] shouldBe episode4
            harness.historyEntries().shouldBeEmpty()
        }

        test("leaves already-watched earlier Episodes untouched") {
            val ep1 = MediaMockups.episode1.copy(status = Status.WATCHED, currentTime = 0L)
            val ep2 = MediaMockups.episode2.copy(status = Status.IS_WATCHING, currentTime = 90_000L)
            val ep3 = MediaMockups.episode3.copy(status = Status.WATCHED, currentTime = 0L)
            harness.seedShow(episodes = listOf(ep1, ep2, ep3))

            val savedIds = mutableListOf<Long>()
            harness.onSaveMedias = { medias -> savedIds += medias.map { it.mediaId } }

            harness.tracker.markPreviousAsWatched(episode = ep3)

            savedIds shouldBe listOf(ep2.id)
            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == ep2.id } shouldBe
                ep2.copy(status = Status.WATCHED, currentTime = 0L)
        }

        test("returns early without writing when all earlier Episodes are already watched or on first Episode") {
            harness.seedShow(
                episodes = listOf(
                    MediaMockups.episode1.copy(status = Status.WATCHED),
                    MediaMockups.episode2.copy(status = Status.WATCHED),
                    MediaMockups.episode3.copy(status = Status.WATCHED),
                )
            )
            harness.onSaveMedias = { error("saveMedias must not be called when nothing needs updating") }

            harness.tracker.markPreviousAsWatched(episode = MediaMockups.episode3)
            harness.tracker.markPreviousAsWatched(episode = MediaMockups.episode1)
        }

        test("returns early for Unknown Artwork and leaves earlier Unknown Episodes and History untouched") {
            val unknown1 = MediaMockups.unknownEpisode.copy(id = -101L, season = 1, number = 1, status = Status.TO_WATCH)
            val unknown2 = Episode(
                file = UserFile(
                    name = "unknown episode S01E02.mkv",
                    addedDateTime = 0L,
                    path = "path/unknown_episode_S01E02.mkv",
                    source = FileSource.LOCAL,
                ),
                duration = 23,
            ).copy(id = -102L, season = 1, number = 2, status = Status.WATCHED)
            harness.seedUnknown(episodes = listOf(unknown1, unknown2))
            harness.onSaveMedias = { error("saveMedias must not be called for Unknown Artwork") }

            harness.tracker.markPreviousAsWatched(episode = unknown2)

            harness.database.getUnknownMedias().first { it.id == unknown1.id }.status shouldBe Status.TO_WATCH
            harness.historyEntries().shouldBeEmpty()
        }

        test("never writes to History even when Show already has a History entry") {
            val ep1 = MediaMockups.episode1.copy(status = Status.TO_WATCH)
            val ep2 = MediaMockups.episode2.copy(status = Status.WATCHED)
            val ep3 = MediaMockups.episode3.copy(status = Status.IS_WATCHING, currentTime = 45_000L)
            harness.seedShow(episodes = listOf(ep1, ep2, ep3))
            harness.history.insert(media = ep3)
            val historyBefore = harness.historyEntries()

            harness.tracker.markPreviousAsWatched(episode = ep2)

            harness.historyEntries() shouldBe historyBefore
        }

        test("Private Show markPreviousAsWatched marks earlier Episodes without writing to History") {
            harness.seedShow(artwork = MediaMockups.showArtwork.copy(isPrivate = true))

            harness.tracker.markPreviousAsWatched(episode = MediaMockups.episode3)

            val saved = harness.database.getEpisodes(MediaMockups.showArtwork.id).associateBy { it.id }
            saved[MediaMockups.episode1.id]?.status shouldBe Status.WATCHED
            saved[MediaMockups.episode2.id]?.status shouldBe Status.WATCHED
            harness.historyEntries().shouldBeEmpty()
        }

    }

    context("resetProgress") {

        test("resets a watching Movie to TO_WATCH at zero and removes it from History") {
            val watchingMovie = MediaMockups.movie.copy(status = Status.IS_WATCHING, currentTime = 120_000L)
            harness.seedMovie(movie = watchingMovie)
            harness.history.insert(media = watchingMovie)

            harness.tracker.resetProgress(artwork = MediaMockups.movieArtwork)

            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.TO_WATCH, currentTime = 0L)
            harness.historyEntries().shouldBeEmpty()
        }

        test("resets a watched Movie or Movie with non-zero currentTime to TO_WATCH at zero") {
            val watchedMovie = MediaMockups.movie.copy(status = Status.WATCHED, currentTime = 0L)
            harness.seedMovie(movie = watchedMovie)

            harness.tracker.resetProgress(artwork = MediaMockups.movieArtwork)

            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.TO_WATCH, currentTime = 0L)

            val nonZeroToWatchMovie = MediaMockups.movie.copy(status = Status.TO_WATCH, currentTime = 30_000L)
            harness.database.saveMedias(listOf(nonZeroToWatchMovie))

            harness.tracker.resetProgress(artwork = MediaMockups.movieArtwork)

            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.TO_WATCH, currentTime = 0L)
        }

        test("skips DB write for a Movie already TO_WATCH at zero while still deleting History") {
            harness.seedMovie(movie = MediaMockups.movie.copy(status = Status.TO_WATCH, currentTime = 0L))
            harness.history.insert(media = MediaMockups.movie)
            harness.onSaveMedias = { error("saveMedias must not be called when Movie is already TO_WATCH at zero") }

            harness.tracker.resetProgress(artwork = MediaMockups.movieArtwork)

            harness.database.getMovie(MediaMockups.movieArtwork.id) shouldBe
                MediaMockups.movie.copy(status = Status.TO_WATCH, currentTime = 0L)
            harness.historyEntries().shouldBeEmpty()
        }

        test("resets a whole Show across seasons, skips Episodes already TO_WATCH at zero, and removes Show from History") {
            val ep1 = MediaMockups.episode1.copy(status = Status.TO_WATCH, currentTime = 0L)
            val ep2 = MediaMockups.episode2.copy(status = Status.IS_WATCHING, currentTime = 120_000L)
            val ep3 = MediaMockups.episode3.copy(status = Status.WATCHED, currentTime = 0L)
            harness.seedShow(episodes = listOf(ep1, ep2, ep3))
            harness.history.insert(media = ep2)

            val savedIds = mutableListOf<Long>()
            harness.onSaveMedias = { medias -> savedIds += medias.map { it.mediaId } }

            harness.tracker.resetProgress(artwork = MediaMockups.showArtwork)

            savedIds shouldBe listOf(ep2.id, ep3.id)
            val persisted = harness.database.getEpisodes(MediaMockups.showArtwork.id).associateBy { it.id }
            persisted[ep1.id] shouldBe ep1
            persisted[ep2.id] shouldBe MediaMockups.episode2.copy(status = Status.TO_WATCH, currentTime = 0L)
            persisted[ep3.id] shouldBe MediaMockups.episode3.copy(status = Status.TO_WATCH, currentTime = 0L)
            harness.historyEntries().shouldBeEmpty()
        }

        test("skips DB write for a whole Show when all Episodes are already TO_WATCH at zero while deleting History") {
            harness.seedShow()
            harness.history.insert(media = MediaMockups.episode1)
            harness.onSaveMedias = { error("saveMedias must not be called when all Episodes are already TO_WATCH at zero") }

            harness.tracker.resetProgress(artwork = MediaMockups.showArtwork)

            harness.historyEntries().shouldBeEmpty()
        }

        test("resets a single Season only, leaves other Seasons untouched, and unconditionally deletes Show History") {
            val ep1 = MediaMockups.episode1.copy(status = Status.TO_WATCH, currentTime = 0L)
            val ep2 = MediaMockups.episode2.copy(status = Status.IS_WATCHING, currentTime = 100_000L)
            val ep3 = MediaMockups.episode3.copy(status = Status.WATCHED, currentTime = 0L)
            harness.seedShow(episodes = listOf(ep1, ep2, ep3))
            harness.seedMovie(artwork = MediaMockups.movieArtwork2, movie = MediaMockups.movie2.copy(status = Status.IS_WATCHING, currentTime = 50_000L))
            // History points to a Season 2 Episode and another Artwork has its own History entry
            harness.history.insert(media = ep3)
            harness.history.insert(media = MediaMockups.movie2.copy(status = Status.IS_WATCHING, currentTime = 50_000L))

            val savedIds = mutableListOf<Long>()
            harness.onSaveMedias = { medias -> savedIds += medias.map { it.mediaId } }

            harness.tracker.resetProgress(artwork = MediaMockups.showArtwork, season = 1)

            savedIds shouldBe listOf(ep2.id)
            val persisted = harness.database.getEpisodes(MediaMockups.showArtwork.id).associateBy { it.id }
            persisted[ep1.id] shouldBe ep1
            persisted[ep2.id] shouldBe MediaMockups.episode2.copy(status = Status.TO_WATCH, currentTime = 0L)
            persisted[ep3.id] shouldBe ep3
            harness.historyEntries().single().artworkId shouldBe MediaMockups.movieArtwork2.id
        }

        test("single Season reset with already-reset Season Episodes skips DB write and still deletes History") {
            val ep1 = MediaMockups.episode1.copy(status = Status.TO_WATCH, currentTime = 0L)
            val ep2 = MediaMockups.episode2.copy(status = Status.TO_WATCH, currentTime = 0L)
            val ep3 = MediaMockups.episode3.copy(status = Status.IS_WATCHING, currentTime = 80_000L)
            harness.seedShow(episodes = listOf(ep1, ep2, ep3))
            harness.history.insert(media = ep3)
            harness.onSaveMedias = { error("saveMedias must not be called when Season 1 is already TO_WATCH at zero") }

            harness.tracker.resetProgress(artwork = MediaMockups.showArtwork, season = 1)

            harness.database.getEpisodes(MediaMockups.showArtwork.id).first { it.id == ep3.id } shouldBe ep3
            harness.historyEntries().shouldBeEmpty()
        }

    }

})
