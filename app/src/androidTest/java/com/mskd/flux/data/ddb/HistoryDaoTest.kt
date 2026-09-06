package com.mskd.flux.data.ddb

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.dao.ArtworkDao
import com.mskd.flux.core.database.data.dao.MediasDao
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.features.history.data.dao.HistoryDao
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.mockups.MediaMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Clock

@RunWith(AndroidJUnit4::class)
@MediumTest
class HistoryDaoTest {

    private lateinit var database: FluxDatabase
    private lateinit var historyDao: HistoryDao
    private lateinit var artworkDao: ArtworkDao
    private lateinit var mediasDao: MediasDao

    @Before
    fun setUpDatabase() = runTest {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FluxDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()

        historyDao = database.historyDao()
        artworkDao = database.artworkDao()
        mediasDao = database.mediasDao()

        seedCatalog()
    }

    private suspend fun seedCatalog() {
        artworkDao.insertArtworks(MediaMockups.artworks.map { it.toEntity() })
        mediasDao.insertOrUpdate(MediaMockups.allMedias.map { it.toEntity() })
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    //region Insert/Update

    @Test
    fun upsert_should_insert_new_entries() = runTest {
        // Given
        val media = MediaMockups.episode1
        val entity = HistoryEntity(
            artworkId = media.artworkId,
            mediaId = media.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )

        // When
        historyDao.upsert(entity = entity)

        // Then
        val result = historyDao.getAll()
        assertEquals(1, result.size)
        assertTrue(result.contains(entity))
    }

    @Test
    fun upsert_should_update_old_entries() = runTest {
        // Given
        val media = MediaMockups.episode1
        val entity = HistoryEntity(
            artworkId = media.artworkId,
            mediaId = media.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        historyDao.upsert(entity)

        // When
        val updatedEntity = entity.copy(timestamp = Clock.System.now().toEpochMilliseconds())
        historyDao.upsert(entity = updatedEntity)

        // Then
        val result = historyDao.getAll()
        assertEquals(1, result.size)
        assertTrue(result.contains(updatedEntity))
    }

    @Test
    fun upsert_should_insert_only_one_entity_by_artwork_id() = runTest {
        // Given
        val media1 = MediaMockups.episode1
        val entity1 = HistoryEntity(
            artworkId = media1.artworkId,
            mediaId = media1.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        val media2 = MediaMockups.episode2
        val entity2 = HistoryEntity(
            artworkId = media2.artworkId,
            mediaId = media2.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )

        // When
        historyDao.upsert(entity = entity1)
        historyDao.upsert(entity = entity2)

        // Then
        val result = historyDao.getAll()
        assertEquals(1, result.size)
        assertTrue(result.contains(entity2))
    }

    //endregion

    //region Flow

    @Test
    fun flow_should_emit_empty_list_when_history_is_empty() = runTest {

        // Given & When
        historyDao.flow().test {
            val initial = awaitItem()

            // Then
            assertTrue(initial.isEmpty())

            cancelAndConsumeRemainingEvents()
        }

    }

    fun flow_should_emit_projection_with_joined_artwork_title_after_upsert() = runTest {
        // Given
        val media = MediaMockups.episode1
        val entity = HistoryEntity(
            artworkId = media.artworkId,
            mediaId = media.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        val expectedArtwork = MediaMockups.artworks.first { it.id == media.artworkId }

        historyDao.flow().test {
            awaitItem() // Empty state

            // When
            historyDao.upsert(entity)

            // Then
            val projections = awaitItem()
            val projection = projections.single()
            assertEquals(media.artworkId, projection.history.artworkId)
            assertEquals(media.mediaId, projection.history.mediaId)
            assertEquals(expectedArtwork.title, projection.artworkTitle)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flow_should_emit_entries_ordered_by_timestamp_descending() = runTest {

        // Given
        val older = MediaMockups.episode1
        val newer = MediaMockups.movie

        historyDao.flow().test {
            awaitItem()

            // When
            historyDao.upsert(HistoryEntity(older.artworkId, older.mediaId, timestamp = 1000L))
            awaitItem()
            historyDao.upsert(HistoryEntity(newer.artworkId, newer.mediaId, timestamp = 2000L))

            // Then
            val projections = awaitItem()
            val orderedArtworkIds = projections.map { it.history.artworkId }
            assertEquals(listOf(newer.artworkId, older.artworkId), orderedArtworkIds)

            cancelAndConsumeRemainingEvents()
        }

    }

    //endregion

    //region Delete

    @Test
    fun delete_should_delete_the_wanted_entity() = runTest {
        // Given
        val media1 = MediaMockups.episode1
        val media2 = MediaMockups.movie
        val entity1 = HistoryEntity(
            artworkId = media1.artworkId,
            mediaId = media1.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        val entity2 = HistoryEntity(
            artworkId = media2.artworkId,
            mediaId = media2.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        historyDao.upsert(entity1)
        historyDao.upsert(entity2)

        // When
        historyDao.delete(artworkId = media1.artworkId)

        // Then
        val result = historyDao.getAll()
        assertEquals(1, result.size)
        assertTrue(result.contains(entity2))
    }

    @Test
    fun clear_should_delete_all_entities() = runTest {
        // Given
        val media1 = MediaMockups.episode1
        val media2 = MediaMockups.movie
        val entity1 = HistoryEntity(
            artworkId = media1.artworkId,
            mediaId = media1.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        val entity2 = HistoryEntity(
            artworkId = media2.artworkId,
            mediaId = media2.mediaId,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        historyDao.upsert(entity1)
        historyDao.upsert(entity2)

        // When
        historyDao.clear()

        // Then
        val result = historyDao.getAll()
        assertTrue(result.isEmpty())
    }

    //endregion

}