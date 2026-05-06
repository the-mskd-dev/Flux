package com.mskd.flux.useCases

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.useCases.mediaProgress.ArtworkProgressUC
import com.mskd.flux.useCases.mediaProgress.ArtworkProgressUCImpl
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.minToMs
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class MediaProgressUCTest : FunSpec({

    fluxExtensions()

    lateinit var artworkRepository: ArtworkRepository
    lateinit var userRepository: UserRepository
    lateinit var artworkProgressUC: ArtworkProgressUC

    beforeTest {

        artworkRepository = mockk(relaxed = true)

        userRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserRepository.State())
        }

        artworkProgressUC = ArtworkProgressUCImpl(
            artworkRepository = artworkRepository,
            userRepository = userRepository,
        )

    }

    context("save progress") {
        withData(
            nameFn = { it.description },
            MediaProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the middle",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            MediaProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the end",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            ),
            MediaProgressUCTestCases.SaveProgress(
                description = "Show - save time at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            MediaProgressUCTestCases.SaveProgress(
                description = "Show - save time at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.WATCHED
            ),
            MediaProgressUCTestCases.SaveProgress(
                description = "Show - save time for last episode at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                progress = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            MediaProgressUCTestCases.SaveProgress(
                description = "Show - save time for last episode at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                progress = MediaMockups.episodes.lastEpisode.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            )
        ) { testCase ->

            val content = when (testCase.media) {
                is Episode -> ArtworkRepository.Content.SHOW(
                    artwork = testCase.artwork,
                    episodes = MediaMockups.episodes
                )
                is Movie -> ArtworkRepository.Content.MOVIE(
                    artwork = testCase.artwork,
                    movie = MediaMockups.movie
                )
            }

            // Given
            artworkRepository = mockk(relaxed = true) {
                every { flow } returns MutableStateFlow(content)
            }

            artworkProgressUC = ArtworkProgressUCImpl(
                artworkRepository = artworkRepository,
                userRepository = userRepository,
            )

            artworkProgressUC.saveProgress(media = testCase.media, progress = testCase.progress)

            when (testCase.media) {
                is Episode -> coVerify { artworkRepository.saveEpisode(any()) }
                is Movie -> coVerify { artworkRepository.saveMovie(any()) }
            }

            if (testCase.shouldBeAddedToRecentlyWatched) {
                coVerify { userRepository.addToRecentlyWatched(testCase.artwork.id) }
            } else {
                coVerify { userRepository.removeFromRecentlyWatched(testCase.artwork.id) }
            }

        }
    }

    test("change status") {

    }

    test("mark previous episodes as watched") {

    }

    test("reset progress") {

    }

})