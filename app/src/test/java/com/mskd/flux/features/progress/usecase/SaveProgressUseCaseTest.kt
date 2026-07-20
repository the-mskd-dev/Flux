package com.mskd.flux.features.progress.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.progress.fake.ProgressUCTestCases
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow

class SaveProgressUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var databaseRepository: DatabaseRepository
    lateinit var userDataStore: UserDataStore
    lateinit var saveProgress: SaveProgressUseCase

    beforeTest {

        databaseRepository = spyk(FakeDatabaseRepository())

        userDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserDataStore.State())
        }

        saveProgress = SaveProgressUseCase(
            database = databaseRepository,
            user = userDataStore,
        )

    }

    context("save progress") {
        withData(
            nameFn = { it.description },
            ProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the middle",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the end",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.WATCHED
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time for last episode at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                progress = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            ProgressUCTestCases.SaveProgress(
                description = "Show - save time for last episode at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                progress = MediaMockups.episodes.lastEpisode.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            )
        ) { testCase ->

            saveProgress(media = testCase.media, progress = testCase.progress)

            when (testCase.media) {
                is Episode -> coVerify { databaseRepository.saveEpisodes(any()) }
                is Movie -> coVerify { databaseRepository.saveMovies(any()) }
            }

            if (testCase.shouldBeAddedToRecentlyWatched) {
                coVerify { userDataStore.addToRecentlyWatched(testCase.artwork.id) }
            } else {
                coVerify { userDataStore.removeFromRecentlyWatched(testCase.artwork.id) }
            }

        }
    }

    test("saveProgress with unknown episode does not affect recently watched") {
        saveProgress(media = MediaMockups.unknownEpisode, progress = 1000L)

        // Verify it saves to database
        coVerify { databaseRepository.saveEpisodes(match { it.any { e -> e.id == MediaMockups.unknownEpisode.id } }) }

        // Verify it does NOT call addToRecentlyWatched or removeFromRecentlyWatched
        coVerify(exactly = 0) { userDataStore.addToRecentlyWatched(any()) }
        coVerify(exactly = 0) { userDataStore.removeFromRecentlyWatched(any()) }
    }

})
