package com.mskd.flux.features.history.domain.repository

import app.cash.turbine.test
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.history.data.dao.HistoryDao
import com.mskd.flux.features.history.data.mapper.toDomain
import com.mskd.flux.features.history.data.mapper.toHistoryEntity
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.history.data.model.HistoryProjection
import com.mskd.flux.features.history.data.repository.HistoryRepositoryImpl
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.assertEquals
import kotlin.time.Clock

class HistoryRepositoryImplTest: FunSpec ({

    lateinit var repository: HistoryRepositoryImpl
    lateinit var historyDao: HistoryDao

    beforeTest {

        historyDao = mockk(relaxed = true)
        repository = HistoryRepositoryImpl(dao = historyDao)

    }

    test("flow should returns a list of HistoryEntry") {

        // Given
        val media = MediaMockups.episode1
        val artwork = MediaMockups.artworks.first { it.id == media.artworkId }
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val historyEntity = HistoryEntity(
            artworkId = media.artworkId,
            mediaId = media.mediaId,
            timestamp = timestamp
        )
        val historyProjection = HistoryProjection(
            history = historyEntity,
            media = media.toEntity(),
            artworkTitle = artwork.title
        )
        every { historyDao.flow() } returns MutableStateFlow(listOf(historyProjection))

        repository.flow.test {

            // When
            val result = awaitItem()

            // Then
            val entry = result.single()
            assertEquals(media, entry.media)
            assertEquals(artwork.title, entry.title)
            assertEquals(timestamp, entry.timestamp)

            cancelAndConsumeRemainingEvents()
        }

    }

    test("insert should convert media to HistoryEntity then call upsert from dao") {

        // Given
        val media = MediaMockups.episode1
        val entity = media.toHistoryEntity()

        // When
        repository.insert(media = media)

        // Then
        coVerify { historyDao.upsert(entity = entity) }

    }

    test("delete should call delete from dao") {

        // Given
        val artworkId = MediaMockups.episode1.artworkId

        // When
        repository.delete(artworkId = artworkId)

        // Then
        coVerify { historyDao.delete(artworkId = artworkId) }

    }

    test("clear should call clear from dao") {

        // Given & When
        repository.clear()

        // Then
        coVerify { historyDao.clear() }

    }

})