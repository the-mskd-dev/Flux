package com.mskd.flux.data.ddb

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.core.database.data.DatabaseDao
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.core.database.data.model.projections.ArtworkImagesProjection
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
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
class DatabaseTest {

    private lateinit var database: FluxDatabase
    private lateinit var dao: DatabaseDao

    @Before
    fun setUpDatabase() {
        database =
                Room.inMemoryDatabaseBuilder(
                                ApplicationProvider.getApplicationContext(),
                                FluxDatabase::class.java
                        )
                        .allowMainThreadQueries()
                        .build()

        dao = database.dao()
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
        dao.insertArtworks(artworks)

        // Then
        val result = dao.getArtworks()
        assertEquals(artworks.size, result.size)
        assertTrue(result.containsAll(artworks))
    }

    @Test
    fun insertMedias_and_getMedias_returns_all_inserted() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }

        // When
        dao.insertMedias(medias)

        // Then
        val result = dao.getMedias()
        assertEquals(medias.size, result.size)
        assertTrue(result.containsAll(medias))
    }

    @Test
    fun insertSeasons_and_getSeasons_returns_all_inserted() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }

        // When
        dao.insertSeasons(seasons)

        // Then
        val result = dao.getSeasons()
        assertEquals(seasons.size, result.size)
        assertTrue(result.containsAll(seasons))
    }

    //endregion

    //region Flow

    @Test
    fun flowArtworks_returns_all_inserted_artworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }

        // When
        dao.insertArtworks(artworks)

        // Then
        dao.flowArtworks().test {
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
        dao.flowArtwork(incorrectId).test {
            val result = awaitItem()
            assertNull(result)
        }
    }

    @Test
    fun flowMedias_returns_all_inserted_medias_by_artwork_id() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedMedias = medias.filter { it.artworkId == showArtworkId }

        // When
        dao.insertMedias(medias)

        // Then
        dao.flowMedias(showArtworkId).test {
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
        dao.insertMedias(medias)

        // Then
        dao.flowMedias(incorrectId).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun flowSeasons_returns_all_inserted_seasons_by_artwork_id() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedSeasons = seasons.filter { it.artworkId == showArtworkId }

        // When
        dao.insertSeasons(seasons)

        // Then
        dao.flowSeasons(showArtworkId).test {
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
        dao.insertSeasons(seasons)

        // Then
        dao.flowSeasons(incorrectId).test {
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
        dao.insertMedias(medias)

        // When
        val result = dao.getMedias(showArtworkId)

        // Then
        assertEquals(expectedMedias.size, result.size)
        assertTrue(result.containsAll(expectedMedias))
    }

    @Test
    fun getSeasons_by_artwork_id_returns_only_matching_seasons() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedSeasons = seasons.filter { it.artworkId == showArtworkId }
        dao.insertSeasons(seasons)

        // When
        val result = dao.getSeasons(showArtworkId)

        // Then
        assertEquals(expectedSeasons.size, result.size)
        assertTrue(result.containsAll(expectedSeasons))
    }

    @Test
    fun getUnknownMedias_returns_only_unknown_medias() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        val expectedUnknowns = medias.filter { it.artworkId == Artwork.UNKNOWN_ID }
        dao.insertMedias(medias)

        // When
        val result = dao.getUnknownMedias()

        // Then
        assertEquals(expectedUnknowns.size, result.size)
        assertTrue(result.containsAll(expectedUnknowns))
    }

    @Test
    fun getMediasNotInFiles_returns_only_medias_where_file_dont_match() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)
        val fileNamesToKeep = listOf(MediaMockups.movie.file.name, MediaMockups.episode1.file.name)
        val expectedMedias = medias.filter { it.fileName !in fileNamesToKeep }

        // When
        val result = dao.getMediasNotInFiles(fileNamesToKeep)

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
        dao.insertMedias(medias)
        val targetMedia = medias.first()
        val updatedFile = MediaMockups.movie.file.copy(path = targetMedia.path, realPath = "/real/storage/path.mkv")

        // When
        dao.updateRealPaths(listOf(updatedFile))

        // Then
        val result = dao.getMedias()
        val updatedMediaInDb = result.find { it.path == targetMedia.path }
        assertEquals("/real/storage/path.mkv", updatedMediaInDb?.realPath)
    }

    //endregion

    //region Delete

    @Test
    fun deleteArtworks_delete_all_artworks_with_given_artwork_ids() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        dao.insertArtworks(artworks)
        val idsToDelete = listOf(MediaMockups.movieArtwork.id)

        // When
        dao.deleteArtworks(idsToDelete)

        // Then
        val result = dao.getArtworks()
        assertEquals(artworks.size - idsToDelete.size, result.size)
        assertTrue(result.none { idsToDelete.contains(it.id) })
    }

    @Test
    fun deleteEmptyArtworks_delete_all_artworks_with_no_matching_medias() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        val movieMedia = MediaMockups.movie.toEntity()
        dao.insertArtworks(artworks)
        dao.insertMedias(listOf(movieMedia))

        // When
        dao.deleteEmptyArtworks()

        // Then
        val result = dao.getArtworks()
        assertEquals(1, result.size)
        assertEquals(MediaMockups.movieArtwork.id, result.first().id)
    }

    @Test
    fun deleteMediasByArtworkIds_delete_medias_with_given_artwork_ids() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)
        val idsToDelete = listOf(MediaMockups.showArtwork.id)

        // When
        dao.deleteMediasByArtworkIds(idsToDelete)

        // Then
        val result = dao.getMedias()
        assertTrue(result.none { idsToDelete.contains(it.artworkId) })
    }

    @Test
    fun deleteEpisodesByIds_delete_only_medias_with_given_ids_and_show_type() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)
        val episodeIdToDelete = MediaMockups.episode1.id

        // When
        dao.deleteEpisodesByIds(listOf(episodeIdToDelete))

        // Then
        val result = dao.getMedias()
        assertTrue(result.none { it.id == episodeIdToDelete && it.type == ContentType.SHOW })
    }

    @Test
    fun deleteSeasons_delete_all_seasons_with_given_artwork_ids() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        dao.insertSeasons(seasons)
        val idsToDelete = listOf(MediaMockups.showArtwork.id)

        // When
        dao.deleteSeasonsByArtworkIds(idsToDelete)

        // Then
        val result = dao.getSeasons()
        assertTrue(result.none { idsToDelete.contains(it.artworkId) })
    }

    @Test
    fun deleteEmptySeasons_delete_all_seasons_with_no_matching_medias() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        val medias = MediaMockups.episodes.map { it.toEntity() }
        dao.insertSeasons(seasons)
        dao.insertMedias(medias)

        // When
        dao.deleteEmptySeasons()

        // Then
        val result = dao.getSeasons()
        assertEquals(2, result.size)
        assertTrue(result.none { it.season == 3 })
    }

    @Test
    fun deleteSeason_delete_season_from_artwork_id_and_season_number() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        dao.insertSeasons(seasons)
        val artworkId = MediaMockups.showArtwork.id
        val seasonNumber = 1

        // When
        dao.deleteSeason(artworkId, seasonNumber)

        // Then
        val result = dao.getSeasons()
        assertTrue(result.none { it.artworkId == artworkId && it.season == seasonNumber })
    }

    @Test
    fun deleteAllArtworks_delete_all_artworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        dao.insertArtworks(artworks)

        // When
        dao.deleteAllArtworks()

        // Then
        val result = dao.getArtworks()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteAllSeasons_delete_all_seasons() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        dao.insertSeasons(seasons)

        // When
        dao.deleteAllSeasons()

        // Then
        val result = dao.getSeasons()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteAllMedias_delete_all_medias() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)

        // When
        dao.deleteAllMedias()

        // Then
        val result = dao.getMedias()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteMediasInFolder_delete_medias_contained_in_the_given_folder_path() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)
        val folderPath = "path/naruto"

        // When
        dao.deleteMediasInFolder(folderPath)

        // Then
        val result = dao.getMedias()
        assertTrue(result.none { it.path.startsWith(folderPath) })
        assertTrue(result.any { it.path == MediaMockups.movie.file.path })
    }

    //endregion

    //region Count

    @Test
    fun getEpisodeCountByArtworkId_returns_count_of_medias_with_given_id_and_show_type() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)
        val showArtworkId = MediaMockups.showArtwork.id

        // When
        val count = dao.getEpisodeCountByArtworkId(showArtworkId)

        // Then
        val expectedCount = medias.count { it.artworkId == showArtworkId && it.type == ContentType.SHOW }
        assertEquals(expectedCount, count)
    }

    @Test
    fun getEpisodeCountBySeason_returns_count_of_seasons_with_given_id_and_season_number() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)
        val showArtworkId = MediaMockups.showArtwork.id
        val seasonNumber = 1

        // When
        val count = dao.getEpisodeCountBySeason(showArtworkId, seasonNumber)

        // Then
        val expectedCount = medias.count { it.artworkId == showArtworkId && it.season == seasonNumber && it.type == ContentType.SHOW }
        assertEquals(expectedCount, count)
    }

    //endregion

    //region Images

    @Test
    fun getArtworksImages_returns_all_ArtworkImagesProjection_from_artworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks.map { it.toEntity() }
        dao.insertArtworks(artworks)

        // When
        val result = dao.getArtworksImages()

        // Then
        val expected = artworks.map { ArtworkImagesProjection(it.imagePath, it.bannerPath) }
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun getMediasImages_returns_all_non_null_imagePaths_from_medias() = runTest {
        // Given
        val medias = MediaMockups.allMedias.map { it.toEntity() }
        dao.insertMedias(medias)

        // When
        val result = dao.getMediasImages()

        // Then
        val expected = medias.mapNotNull { it.imagePath }
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    @Test
    fun getSeasonsImages_returns_all_non_null_imagePaths_from_seasons() = runTest {
        // Given
        val seasons = MediaMockups.seasons.map { it.toEntity() }
        dao.insertSeasons(seasons)

        // When
        val result = dao.getSeasonsImages()

        // Then
        val expected = seasons.mapNotNull { it.imagePath }
        assertEquals(expected.size, result.size)
        assertTrue(result.containsAll(expected))
    }

    //endregion
}

