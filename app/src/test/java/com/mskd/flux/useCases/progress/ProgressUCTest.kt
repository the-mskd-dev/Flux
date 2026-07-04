package com.mskd.flux.useCases.progress

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.core.datastore.user.UserDataStore
import com.mskd.flux.data.useCases.progress.ProgressUC
import com.mskd.flux.data.useCases.progress.ProgressUCImpl
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.mockkDatabaseRepository
import com.mskd.flux.model.domain.artwork.ContentType
import com.mskd.flux.model.domain.artwork.Episode
import com.mskd.flux.model.domain.artwork.Movie
import com.mskd.flux.model.domain.artwork.Status
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class ProgressUCTest : FunSpec({

    fluxExtensions()

    lateinit var databaseRepository: DatabaseRepository
    lateinit var userDataStore: UserDataStore
    lateinit var progressUC: ProgressUC

    beforeTest {

        databaseRepository = mockkDatabaseRepository()

        userDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserDataStore.State())
        }

        progressUC = ProgressUCImpl(
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

            progressUC.saveProgress(media = testCase.media, progress = testCase.progress)

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

    context("change status") {
       withData(
           nameFn = { it.description },
           ProgressUCTestCases.ChangeStatus(
               description = "Change movie as watched",
               media = MediaMockups.movie,
               status = Status.WATCHED,
               expectedRemoveFromRecentlyWatched = true
           ),
           ProgressUCTestCases.ChangeStatus(
               description = "Change movie as not watched",
               media = MediaMockups.movie,
               status = Status.TO_WATCH,
               expectedRemoveFromRecentlyWatched = false
           ),
           ProgressUCTestCases.ChangeStatus(
               description = "Change episode 2 as watched",
               media = MediaMockups.episode2,
               status = Status.WATCHED,
               expectedRemoveFromRecentlyWatched = false
           ),
           ProgressUCTestCases.ChangeStatus(
               description = "Change last episode as watched",
               media = MediaMockups.episode3,
               status = Status.WATCHED,
               expectedRemoveFromRecentlyWatched = true
           ),
           ProgressUCTestCases.ChangeStatus(
               description = "Change last episode as not watched",
               media = MediaMockups.episode3,
               status = Status.TO_WATCH,
               expectedRemoveFromRecentlyWatched = false
           )
       ) { testCase ->

           progressUC.changeMediaStatus(
               media = testCase.media,
               status = testCase.status
           )

           when (testCase.media) {
               is Episode -> {
                   coVerify { databaseRepository.saveEpisodes(match { it.all { e -> e.id == testCase.media.id } } ) }
               }
               is Movie -> {
                   coVerify { databaseRepository.saveMovies(match { it.all { e -> e.artworkId == testCase.media.artworkId } } ) }
               }
           }

           if (testCase.expectedRemoveFromRecentlyWatched) {
            coVerify { userDataStore.removeFromRecentlyWatched(testCase.media.artworkId) }
           }

       }

    }


    test("mark previous episodes as watched") {

        progressUC.markPreviousEpisodesAsWatchedFor(episode = MediaMockups.episode3)

        coVerify { databaseRepository.saveEpisodes(match { episodes -> episodes.size == 2 && episodes.all { it.status == Status.WATCHED } })  }

    }

    context("reset progress") {
        withData(
            nameFn = { it.description },
            ProgressUCTestCases.ResetProgress(
                description = "reset movie",
                artwork = MediaMockups.movieArtwork,
            ),
            ProgressUCTestCases.ResetProgress(
                description = "reset movie",
                artwork = MediaMockups.showArtwork,
            )
        ) { testCase ->

            progressUC.resetProgress(artwork = testCase.artwork, season = testCase.season)

            when (testCase.artwork.type) {
                ContentType.MOVIE -> {
                    coVerify { databaseRepository.saveMovies(match { movies -> movies.all { it.status == Status.TO_WATCH && it.currentTime == 0L } }) }
                }
                ContentType.SHOW -> {
                    coVerify { databaseRepository.saveEpisodes(match { episodes ->  episodes.all { it.status == Status.TO_WATCH && it.currentTime == 0L } } ) }
                }
            }

            coVerify { userDataStore.removeFromRecentlyWatched(artworkId = testCase.artwork.id) }

        }
    }

    test("reset progress for specific season") {
        val episodes = listOf(
            MediaMockups.episode1.copy(status = Status.WATCHED),
            MediaMockups.episode2.copy(status = Status.IS_WATCHING, currentTime = 1000L),
            MediaMockups.episode3.copy(status = Status.WATCHED)
        )
        coEvery { databaseRepository.getEpisodes(MediaMockups.showArtwork.id) } returns episodes

        progressUC.resetProgress(artwork = MediaMockups.showArtwork, season = 1)

        coVerify {
            databaseRepository.saveEpisodes(match { saved ->
                saved.size == 2 && saved.all { it.season == 1 && it.status == Status.TO_WATCH && it.currentTime == 0L }
            })
        }
    }

    test("saveProgress with unknown episode does not affect recently watched") {
        progressUC.saveProgress(media = MediaMockups.unknownEpisode, progress = 1000L)

        // Verify it saves to database
        coVerify { databaseRepository.saveEpisodes(match { it.any { e -> e.id == MediaMockups.unknownEpisode.id } }) }

        // Verify it does NOT call addToRecentlyWatched or removeFromRecentlyWatched
        coVerify(exactly = 0) { userDataStore.addToRecentlyWatched(any()) }
        coVerify(exactly = 0) { userDataStore.removeFromRecentlyWatched(any()) }
    }

    test("markPreviousEpisodesAsWatchedFor returns early if no previous unwatched episodes") {
        coEvery { databaseRepository.getEpisodes(any()) } returns listOf(
            MediaMockups.episode1.copy(status = Status.WATCHED),
            MediaMockups.episode2.copy(status = Status.WATCHED),
            MediaMockups.episode3.copy(status = Status.WATCHED)
        )

        progressUC.markPreviousEpisodesAsWatchedFor(episode = MediaMockups.episode3)

        // Verify saveEpisodes is never called
        coVerify(exactly = 0) { databaseRepository.saveEpisodes(any()) }
    }

})