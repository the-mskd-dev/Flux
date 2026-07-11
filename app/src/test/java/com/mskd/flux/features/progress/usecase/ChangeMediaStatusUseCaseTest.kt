package com.mskd.flux.features.progress.usecase

import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.fake.ProgressUCTestCases
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow

class ChangeMediaStatusUseCaseTest : FunSpec ({

    lateinit var databaseRepository: DatabaseRepository
    lateinit var userDataStore: UserDataStore
    lateinit var changeMediaStatus: ChangeMediaStatusUseCase

    beforeTest {

        databaseRepository = spyk(FakeDatabaseRepository())

        userDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserDataStore.State())
        }

        changeMediaStatus = ChangeMediaStatusUseCase(
            database = databaseRepository,
            user = userDataStore,
        )

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

            changeMediaStatus(
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

})