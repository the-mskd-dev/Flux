package com.mskd.flux.data.ddb

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.dao.MediasDao
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
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
class MediasDaoTest {

    private lateinit var database: FluxDatabase
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

        mediasDao = database.mediasDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    //region Insert

    @Test
    fun insertMedias_and_getMedias_returns_all_inserted() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }

        // When
        mediasDao.insert(medias)

        // Then
        val result = mediasDao.getAll()
        assertEquals(medias.size, result.size)
        assertTrue(result.containsAll(medias))
    }

    //endregion

    //region Flow

    @Test
    fun flowMedias_returns_all_inserted_medias_by_artwork_id() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedMedias = medias.filter { it.artworkId == showArtworkId }

        // When
        mediasDao.insert(medias)

        // Then
        mediasDao.flow(showArtworkId).test {
            val result = awaitItem()
            assertEquals(expectedMedias.size, result.size)
            assertTrue(result.containsAll(expectedMedias))
        }
    }

    @Test
    fun flowMedias_return_empty_result_when_incorrect_artwork_id() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        val incorrectId = 999L

        // When
        mediasDao.insert(medias)

        // Then
        mediasDao.flow(incorrectId).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
        }
    }

    //endregion

    //region Get

    @Test
    fun getMedias_by_artwork_id_returns_only_matching_medias() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedMedias = medias.filter { it.artworkId == showArtworkId }
        mediasDao.insert(medias)

        // When
        val result = mediasDao.getForArtwork(showArtworkId)

        // Then
        assertEquals(expectedMedias.size, result.size)
        assertTrue(result.containsAll(expectedMedias))
    }

    @Test
    fun getUnknownMedias_returns_only_unknown_medias() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        val expectedUnknowns = medias.filter { it.artworkId == Artwork.UNKNOWN_ID }
        mediasDao.insert(medias)

        // When
        val result = mediasDao.getUnknownMedias()

        // Then
        assertEquals(expectedUnknowns.size, result.size)
        assertTrue(result.containsAll(expectedUnknowns))
    }

    @Test
    fun getMediasNotInFiles_returns_only_medias_where_file_dont_match() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)
        val fileNamesToKeep = listOf(MediaMockups.movie.file.name, MediaMockups.episode1.file.name)
        val expectedMedias = medias.filter { it.fileName !in fileNamesToKeep }

        // When
        val result = mediasDao.getMediasNotInFiles(fileNamesToKeep)

        // Then
        assertEquals(expectedMedias.size, result.size)
        assertTrue(result.containsAll(expectedMedias))
    }

    //endregion

    //region Update

    @Test
    fun updateRealPaths_update_realPath_with_matching_files() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity().copy(realPath = "") }
        mediasDao.insert(medias)
        val targetMedia = medias.first()
        val updatedFile = MediaMockups.movie.file.copy(path = targetMedia.path, realPath = "/real/storage/path.mkv")

        // When
        mediasDao.updateRealPaths(listOf(updatedFile))

        // Then
        val result = mediasDao.getAll()
        val updatedMediaInDb = result.find { it.path == targetMedia.path }
        assertEquals("/real/storage/path.mkv", updatedMediaInDb?.realPath)
    }

    //endregion

    //region Delete

    @Test
    fun deleteMediasByArtworkIds_delete_medias_with_given_artwork_ids() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)
        val idsToDelete = listOf(MediaMockups.showArtwork.id)

        // When
        mediasDao.deleteMediasByArtworkIds(idsToDelete)

        // Then
        val result = mediasDao.getAll()
        assertTrue(result.none { idsToDelete.contains(it.artworkId) })
    }

    @Test
    fun deleteEpisodesByIds_delete_only_medias_with_given_ids_and_show_type() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)
        val episodeIdToDelete = MediaMockups.episode1.id

        // When
        mediasDao.deleteEpisodesByIds(listOf(episodeIdToDelete))

        // Then
        val result = mediasDao.getAll()
        assertTrue(result.none { it.id == episodeIdToDelete && it.type == ContentType.SHOW })
    }

    @Test
    fun deleteAllMedias_delete_all_medias() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)

        // When
        mediasDao.deleteAllMedias()

        // Then
        val result = mediasDao.getAll()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteMediasInFolder_delete_medias_contained_in_the_given_folder_path() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)
        val folderPath = "path/naruto"

        // When
        mediasDao.deleteMediasInFolder(folderPath)

        // Then
        val result = mediasDao.getAll()
        assertTrue(result.none { it.path.startsWith(folderPath) })
        assertTrue(result.any { it.path == MediaMockups.movie.file.path })
    }

    //endregion

    //region Count

    @Test
    fun getEpisodeCountByArtworkId_returns_count_of_medias_with_given_id_and_show_type() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)
        val showArtworkId = MediaMockups.showArtwork.id

        // When
        val count = mediasDao.getEpisodeCountByArtworkId(showArtworkId)

        // Then
        val expectedCount = medias.count { it.artworkId == showArtworkId && it.type == ContentType.SHOW }
        assertEquals(expectedCount, count)
    }

    @Test
    fun getEpisodeCountBySeason_returns_count_of_seasons_with_given_id_and_season_number() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)
        val showArtworkId = MediaMockups.showArtwork.id
        val seasonNumber = 1

        // When
        val count = mediasDao.getEpisodeCountBySeason(showArtworkId, seasonNumber)

        // Then
        val expectedCount = medias.count { it.artworkId == showArtworkId && it.season == seasonNumber && it.type == ContentType.SHOW }
        assertEquals(expectedCount, count)
    }

    //endregion

    //region Images

    @Test
    fun getMediasImages_returns_all_non_null_imagePaths_from_medias() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        mediasDao.insert(medias)

        // When
        val result = mediasDao.getMediasImages()

        // Then
        val expected = medias.mapNotNull { it.imagePath }
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    //endregion

}