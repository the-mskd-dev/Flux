package com.mskd.flux.features.progress.usecase

import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.progress.domain.usecase.MarkPreviousAsWatchedUseCase
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk

class MarkPreviousAsWatchedUseCaseTest : FunSpec({

    lateinit var databaseRepository: DatabaseRepository
    lateinit var markPreviousAsWatched: MarkPreviousAsWatchedUseCase

    beforeTest {

        databaseRepository = spyk(FakeDatabaseRepository())

        markPreviousAsWatched = MarkPreviousAsWatchedUseCase(
            database = databaseRepository
        )

    }

    test("mark previous episodes as watched") {

        markPreviousAsWatched(episode = MediaMockups.episode3)

        coVerify { databaseRepository.saveMedias(match { episodes -> episodes.size == 2 && episodes.all { it.status == Status.WATCHED } })  }

    }

    test("returns early if no previous unwatched episodes") {
        coEvery { databaseRepository.getEpisodes(any()) } returns listOf(
            MediaMockups.episode1.copy(status = Status.WATCHED),
            MediaMockups.episode2.copy(status = Status.WATCHED),
            MediaMockups.episode3.copy(status = Status.WATCHED)
        )

        markPreviousAsWatched(episode = MediaMockups.episode3)

        // Verify saveEpisodes is never called
        coVerify(exactly = 0) { databaseRepository.saveMedias(any()) }
    }

})