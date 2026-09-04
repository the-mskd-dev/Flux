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
import com.mskd.flux.core.database.data.model.projections.ArtworkImagesProjection
import com.mskd.flux.mockups.MediaMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class ArtworkDaoTest {

    private lateinit var database: FluxDatabase
    private lateinit var artworkDao: ArtworkDao
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

        artworkDao = database.artworkDao()
        mediasDao = database.mediasDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    //region Insert

    @Test
    fun insertArtworks_and_getArtworks_returns_all_inserted() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }

        // When
        artworkDao.insertArtworks(artworks)

        // Then
        val result = artworkDao.getArtworks()
        assertEquals(artworks.size, result.size)
        assertTrue(result.containsAll(artworks))
    }

    //endregion

    //region Flow

    @Test
    fun flowArtworks_returns_all_inserted_artworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }

        // When
        artworkDao.insertArtworks(artworks)

        // Then
        artworkDao.flowArtworks().test {
            val result = awaitItem()
            assertEquals(artworks.size, result.size)
            assertTrue(result.containsAll(artworks))
        }
    }

    @Test
    fun flowArtworks_return_empty_result_when_incorrect_artwork_id() = runTest {
        // Given
        val incorrectId = 999L

        // When/Then
        artworkDao.flowArtwork(incorrectId).test {
            val result = awaitItem()
            assertNull(result)
        }
    }

    //endregion


    //region Delete


    @Test
    fun deleteArtworks_delete_all_artworks_with_given_artwork_ids() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        artworkDao.insertArtworks(artworks)
        val idsToDelete = listOf(MediaMockups.movieArtwork.id)

        // When
        artworkDao.deleteArtworks(idsToDelete)

        // Then
        val result = artworkDao.getArtworks()
        assertEquals(artworks.size - idsToDelete.size, result.size)
        assertTrue(result.none { idsToDelete.contains(it.id) })
    }

    @Test
    fun deleteEmptyArtworks_delete_all_artworks_with_no_matching_medias() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        val movieMedia = MediaMockups.movie.toEntity()
        artworkDao.insertArtworks(artworks)
        mediasDao.insert(listOf(movieMedia))

        // When
        artworkDao.deleteEmptyArtworks()

        // Then
        val result = artworkDao.getArtworks()
        assertEquals(1, result.size)
        assertEquals(MediaMockups.movieArtwork.id, result.first().id)
    }

    @Test
    fun deleteAllArtworks_delete_all_artworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        artworkDao.insertArtworks(artworks)

        // When
        artworkDao.deleteAllArtworks()

        // Then
        val result = artworkDao.getArtworks()
        assertTrue(result.isEmpty())
    }

    //endregion

    //region Images

    @Test
    fun getArtworksImages_returns_all_ArtworkImagesProjection_from_artworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        artworkDao.insertArtworks(artworks)

        // When
        val result = artworkDao.getArtworksImages()

        // Then
        val expected = artworks.map { ArtworkImagesProjection(it.imagePath, it.bannerPath) }
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    //endregion

}