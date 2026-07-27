package com.mskd.flux.features.progress.usecase

import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.fake.ProgressUCTestCases
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow

class ResetProgressUseCaseTest : FunSpec({

    lateinit var databaseRepository: DatabaseRepository
    lateinit var userDataStore: UserDataStore
    lateinit var resetProgress: ResetProgressUseCase

    beforeTest {

        databaseRepository = spyk(FakeDatabaseRepository())

        userDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserDataStore.State())
        }

        resetProgress = ResetProgressUseCase(
            database = databaseRepository,
            user = userDataStore,
        )

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

            resetProgress(artwork = testCase.artwork, season = testCase.season)

            when (testCase.artwork.type) {
                ContentType.MOVIE -> {
                    coVerify { databaseRepository.saveMedias(match { movies -> movies.all { it.status == Status.TO_WATCH && it.currentTime == 0L } }) }
                }
                ContentType.SHOW -> {
                    coVerify { databaseRepository.saveMedias(match { episodes ->  episodes.all { it.status == Status.TO_WATCH && it.currentTime == 0L } } ) }
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

        resetProgress(artwork = MediaMockups.showArtwork, season = 1)

        coVerify {
            databaseRepository.saveMedias(match { saved ->
                saved.size == 2 && saved.all { (it as Episode).season == 1 && it.status == Status.TO_WATCH && it.currentTime == 0L }
            })
        }
    }

})