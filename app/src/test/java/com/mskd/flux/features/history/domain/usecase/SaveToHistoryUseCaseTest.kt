package com.mskd.flux.features.history.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SaveToHistoryUseCaseTest : FunSpec({

    lateinit var history: HistoryRepository
    lateinit var database: DatabaseRepository
    lateinit var useCase: SaveToHistoryUseCase

    beforeTest {
        history = mockk(relaxed = true)
        database = mockk(relaxed = true)
        useCase = SaveToHistoryUseCase(history = history, database = database)
    }

    // A non-IS_WATCHING status picked dynamically, so the test doesn't depend
    // on guessing the exact enum member name (WATCHED, NOT_STARTED, etc.).
    val notWatchingStatus = Status.entries.first { it != Status.IS_WATCHING }

    //region Episode

    test("invoke should do nothing when episode is unknown") {
        // Given
        val episode = MediaMockups.unknownEpisode

        // When
        useCase(media = episode)

        // Then
        coVerify(exactly = 0) { history.insert(media = any()) }
        coVerify(exactly = 0) { history.delete(artworkId = any()) }
        coVerify(exactly = 0) { database.getEpisodes(artworkId = any()) }
    }

    test("invoke should insert episode into history when it is currently watching") {
        // Given
        val episode = MediaMockups.episode1.copy(status = Status.IS_WATCHING)

        // When
        useCase(media = episode)

        // Then
        coVerify { history.insert(media = episode) }
        coVerify(exactly = 0) { database.getEpisodes(artworkId = any()) }
    }

    test("invoke should insert next episode into history when current one is finished and a next episode exists") {
        // Given
        val currentEpisode = MediaMockups.episode1.copy(
            status = notWatchingStatus,
            season = 1,
            number = 1
        )
        val nextEpisode = MediaMockups.episode2.copy(
            artworkId = currentEpisode.artworkId,
            season = 1,
            number = 2
        )
        coEvery { database.getEpisodes(artworkId = currentEpisode.artworkId) } returns listOf(currentEpisode, nextEpisode)

        // When
        useCase(media = currentEpisode)

        // Then
        coVerify { history.insert(media = nextEpisode) }
        coVerify(exactly = 0) { history.delete(artworkId = any()) }
    }

    test("invoke should delete history when current episode is finished and no next episode exists") {
        // Given
        val lastEpisode = MediaMockups.episode1.copy(
            status = notWatchingStatus,
            season = 1,
            number = 1
        )
        coEvery { database.getEpisodes(artworkId = lastEpisode.artworkId) } returns listOf(lastEpisode)

        // When
        useCase(media = lastEpisode)

        // Then
        coVerify { history.delete(artworkId = lastEpisode.artworkId) }
        coVerify(exactly = 0) { history.insert(media = any()) }
    }

    //endregion

    //region Movie

    test("invoke should insert movie into history when it is currently watching") {
        // Given
        val movie = MediaMockups.movie.copy(status = Status.IS_WATCHING)

        // When
        useCase(media = movie)

        // Then
        coVerify { history.insert(media = movie) }
    }

    test("invoke should delete history when movie is not currently watching") {
        // Given
        val movie = MediaMockups.movie.copy(status = notWatchingStatus)

        // When
        useCase(media = movie)

        // Then
        coVerify { history.delete(artworkId = movie.artworkId) }
    }

    //endregion

})