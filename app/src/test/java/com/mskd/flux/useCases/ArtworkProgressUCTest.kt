package com.mskd.flux.useCases

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.useCases.artworkProgress.ArtworkProgressUC
import com.mskd.flux.useCases.artworkProgress.ArtworkProgressUCImpl
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

class ArtworkProgressUCTest : FunSpec({

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
            ArtworkProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the middle",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            ArtworkProgressUCTestCases.SaveProgress(
                description = "Movie - save time at the end",
                artwork = MediaMockups.movieArtwork,
                media = MediaMockups.movie,
                progress = MediaMockups.movie.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = false,
                statusExpected = Status.WATCHED
            ),
            ArtworkProgressUCTestCases.SaveProgress(
                description = "Show - save time at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            ArtworkProgressUCTestCases.SaveProgress(
                description = "Show - save time at the end",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                progress = MediaMockups.episode1.duration.minToMs.times(Constants.PLAYER.PROGRESS_THRESHOLD).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.WATCHED
            ),
            ArtworkProgressUCTestCases.SaveProgress(
                description = "Show - save time for last episode at the middle",
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episodes.lastEpisode,
                progress = MediaMockups.episodes.lastEpisode.duration.minToMs.times(0.5).toLong(),
                shouldBeAddedToRecentlyWatched = true,
                statusExpected = Status.IS_WATCHING
            ),
            ArtworkProgressUCTestCases.SaveProgress(
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

    context("change status") {
       withData(
           nameFn = { it.description },
           ArtworkProgressUCTestCases.ChangeStatus(
               description = "Change movie as watched",
               media = MediaMockups.movie,
               status = Status.WATCHED,
               artworkContent = ArtworkRepository.Content.MOVIE(
                   artwork = MediaMockups.movieArtwork,
                   movie = MediaMockups.movie
               ),
               expectedRemoveFromRecentlyWatched = true
           ),
           ArtworkProgressUCTestCases.ChangeStatus(
               description = "Change movie as not watched",
               media = MediaMockups.movie,
               status = Status.TO_WATCH,
               artworkContent = ArtworkRepository.Content.MOVIE(
                   artwork = MediaMockups.movieArtwork,
                   movie = MediaMockups.movie
               ),
               expectedRemoveFromRecentlyWatched = false
           ),
           ArtworkProgressUCTestCases.ChangeStatus(
               description = "Change episode 2 as watched",
               media = MediaMockups.episode2,
               status = Status.WATCHED,
               artworkContent = ArtworkRepository.Content.SHOW(
                   artwork = MediaMockups.showArtwork,
                   episodes = MediaMockups.episodes
               ),
               expectedRemoveFromRecentlyWatched = false
           ),
           ArtworkProgressUCTestCases.ChangeStatus(
               description = "Change last episode as watched",
               media = MediaMockups.episode3,
               status = Status.WATCHED,
               artworkContent = ArtworkRepository.Content.SHOW(
                   artwork = MediaMockups.showArtwork,
                   episodes = MediaMockups.episodes
               ),
               expectedRemoveFromRecentlyWatched = true
           ),
           ArtworkProgressUCTestCases.ChangeStatus(
               description = "Change last episode as not watched",
               media = MediaMockups.episode3,
               status = Status.TO_WATCH,
               artworkContent = ArtworkRepository.Content.SHOW(
                   artwork = MediaMockups.showArtwork,
                   episodes = MediaMockups.episodes
               ),
               expectedRemoveFromRecentlyWatched = false
           )
       ) { testCase ->

           artworkRepository = mockk(relaxed = true) {
               every { flow } returns MutableStateFlow(testCase.artworkContent)
           }

           artworkProgressUC = ArtworkProgressUCImpl(
               artworkRepository = artworkRepository,
               userRepository = userRepository,
           )

           artworkProgressUC.changeMediaStatus(
               media = testCase.media,
               status = testCase.status
           )

           when (testCase.media) {
               is Episode -> {
                   coVerify { artworkRepository.saveEpisode(match { it.id == testCase.media.id }) }
               }
               is Movie -> {
                   coVerify { artworkRepository.saveMovie(match { it.artworkId == testCase.media.artworkId }) }
               }
           }

           if (testCase.expectedRemoveFromRecentlyWatched) {
            coVerify { userRepository.removeFromRecentlyWatched(testCase.media.artworkId) }
           }

       }

    }


    test("mark previous episodes as watched") {

        artworkRepository = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(
                ArtworkRepository.Content.SHOW(
                    artwork = MediaMockups.showArtwork,
                    episodes = MediaMockups.episodes
                )
            )
        }

        artworkProgressUC = ArtworkProgressUCImpl(
            artworkRepository = artworkRepository,
            userRepository = userRepository,
        )

        artworkProgressUC.markPreviousEpisodesAsWatchedFor(episode = MediaMockups.episode3)

        coVerify { artworkRepository.saveEpisodes(match { episodes -> episodes.size == 2 && episodes.all { it.status == Status.WATCHED } })  }

    }

    context("reset progress") {
        withData(
            nameFn = { it.description },
            ArtworkProgressUCTestCases.ResetProgress(
                description = "reset movie",
                artwork = MediaMockups.movieArtwork,
                artworkContent = ArtworkRepository.Content.MOVIE(
                    artwork = MediaMockups.movieArtwork,
                    movie = MediaMockups.movie
                )
            ),
            ArtworkProgressUCTestCases.ResetProgress(
                description = "reset movie",
                artwork = MediaMockups.showArtwork,
                artworkContent = ArtworkRepository.Content.SHOW(
                    artwork = MediaMockups.showArtwork,
                    episodes = MediaMockups.episodesWithStatus
                )
            )
        ) { testCase ->

            artworkRepository = mockk(relaxed = true) {
                coEvery { getArtwork(any()) } returns testCase.artworkContent
            }

            artworkProgressUC = ArtworkProgressUCImpl(
                artworkRepository = artworkRepository,
                userRepository = userRepository,
            )

            artworkProgressUC.resetProgress(artwork = testCase.artwork)

            when (testCase.artworkContent) {
                is ArtworkRepository.Content.MOVIE -> {
                    coVerify { artworkRepository.saveMovie(match { it.status == Status.TO_WATCH && it.currentTime == 0L }) }
                }
                is ArtworkRepository.Content.SHOW -> {
                    coVerify { artworkRepository.saveEpisodes(match { episodes ->  episodes.all { it.status == Status.TO_WATCH && it.currentTime == 0L } } ) }
                }
                ArtworkRepository.Content.ERROR -> { assert(false) }
            }

            coVerify { userRepository.removeFromRecentlyWatched(artworkId = testCase.artwork.id) }

        }
    }

})