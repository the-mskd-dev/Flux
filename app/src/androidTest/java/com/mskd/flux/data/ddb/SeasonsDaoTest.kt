package com.mskd.flux.data.ddb

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.dao.MediasDao
import com.mskd.flux.core.database.data.dao.SeasonsDao
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.mockups.MediaMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class SeasonsDaoTest {

    private lateinit var database: FluxDatabase
    private lateinit var seasonsDao: SeasonsDao
    private lateinit var mediasDao: MediasDao

    @Before
    fun setUpDatabase() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                FluxDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()

        seasonsDao = database.seasonsDao()
        mediasDao = database.mediasDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    //region Insert

    @Test
    fun insertSeasons_and_getSeasons_returns_all_inserted() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }

        // When
        seasonsDao.insert(seasons)

        // Then
        val result = seasonsDao.getAll()
        assertEquals(seasons.size, result.size)
        assertTrue(result.containsAll(seasons))
    }

    //endregion

    //region Flow

    @Test
    fun flowSeasons_returns_all_inserted_seasons_by_artwork_id() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedSeasons = seasons.filter { it.artworkId == showArtworkId }

        // When
        seasonsDao.insert(seasons)

        // Then
        seasonsDao.flow(showArtworkId).test {
            val result = awaitItem()
            assertEquals(expectedSeasons.size, result.size)
            assertTrue(result.containsAll(expectedSeasons))
        }
    }

    @Test
    fun flowSeasons_return_empty_result_when_incorrect_artwork_id() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        val incorrectId = 999L

        // When
        seasonsDao.insert(seasons)

        // Then
        seasonsDao.flow(incorrectId).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
        }
    }

    //endregion

    //region Get

    @Test
    fun getSeasons_by_artwork_id_returns_only_matching_seasons() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedSeasons = seasons.filter { it.artworkId == showArtworkId }
        seasonsDao.insert(seasons)

        // When
        val result = seasonsDao.getForArtwork(showArtworkId)

        // Then
        assertEquals(expectedSeasons.size, result.size)
        assertTrue(result.containsAll(expectedSeasons))
    }

    //endregion

    //region Delete

    @Test
    fun deleteSeasons_delete_all_seasons_with_given_artwork_ids() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        seasonsDao.insert(seasons)
        val idsToDelete = listOf(MediaMockups.showArtwork.id)

        // When
        seasonsDao.deleteByArtworkIds(idsToDelete)

        // Then
        val result = seasonsDao.getAll()
        assertTrue(result.none { idsToDelete.contains(it.artworkId) })
    }

    @Test
    fun deleteEmptySeasons_delete_all_seasons_with_no_matching_medias() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        val medias = MediaMockups.episodes.map { it.toEntity() }
        seasonsDao.insert(seasons)
        mediasDao.insert(medias)

        // When
        seasonsDao.deleteEmptySeasons()

        // Then
        val result = seasonsDao.getAll()
        assertEquals(2, result.size)
        assertTrue(result.none { it.season == 3 })
    }

    @Test
    fun deleteSeason_delete_season_from_artwork_id_and_season_number() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        seasonsDao.insert(seasons)
        val artworkId = MediaMockups.showArtwork.id
        val seasonNumber = 1

        // When
        seasonsDao.delete(artworkId, seasonNumber)

        // Then
        val result = seasonsDao.getAll()
        assertTrue(result.none { it.artworkId == artworkId && it.season == seasonNumber })
    }

    @Test
    fun deleteAllSeasons_delete_all_seasons() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        seasonsDao.insert(seasons)

        // When
        seasonsDao.deleteAll()

        // Then
        val result = seasonsDao.getAll()
        assertTrue(result.isEmpty())
    }

    //endregion

    //region Images

    @Test
    fun getSeasonsImages_returns_all_non_null_imagePaths_from_seasons() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        seasonsDao.insert(seasons)

        // When
        val result = seasonsDao.getImages()

        // Then
        val expected = seasons.mapNotNull { it.imagePath }
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    //endregion
}