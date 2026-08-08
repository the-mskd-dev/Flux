package com.mskd.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.mappers.toEntity
import com.mskd.flux.core.database.data.repository.DatabaseRepositoryImpl
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.utils.extensions.sort
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
class DatabaseRepositoryImplTest {

    private lateinit var database: FluxDatabase
    private lateinit var repository: DatabaseRepositoryImpl

    @Before
    fun setUp() {
        database =
                Room.inMemoryDatabaseBuilder(
                                ApplicationProvider.getApplicationContext(),
                                FluxDatabase::class.java
                        )
                        .allowMainThreadQueries()
                        .build()

        repository =
            DatabaseRepositoryImpl(
                database.dao()
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    //region Flows

    @Test
    fun flowArtworks_returns_flow_of_inserted_artworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks
        repository.saveArtworks(artworks, overrideLastModification = false)

        // When/Then
        repository.flowArtworks().test {
            val result = awaitItem()
            assertEquals(artworks.size, result.size)
            assertTrue(result.containsAll(artworks))
        }
    }

    @Test
    fun flowArtwork_returns_flow_of_artwork_with_given_artwork_id() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        repository.saveArtworks(listOf(artwork), overrideLastModification = false)

        // When/Then
        repository.flowArtwork(artwork.id).test {
            val result = awaitItem()
            assertEquals(artwork, result)
        }
    }

    @Test
    fun flowArtwork_emits_new_value_when_artwork_is_modified_or_inserted() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val updatedArtwork = artwork.copy(title = "Your Name Updated")

        // When/Then
        repository.flowArtwork(artwork.id).test {
            assertEquals(null, awaitItem())

            repository.saveArtworks(listOf(artwork), overrideLastModification = false)
            assertEquals(artwork, awaitItem())

            repository.saveArtworks(listOf(updatedArtwork), overrideLastModification = false)
            assertEquals(updatedArtwork, awaitItem())
        }
    }

    @Test
    fun flowArtwork_returns_null_when_id_doesnt_exist() = runTest {
        // Given
        val incorrectId = 999L

        // When/Then
        repository.flowArtwork(incorrectId).test {
            val result = awaitItem()
            assertNull(result)
        }
    }

    @Test
    fun flowMedias_returns_flow_of_medias_with_given_artwork_id() = runTest {
        // Given
        val medias = MediaMockups.allMedias
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedMedias = medias.filter { it.artworkId == showArtworkId }
        repository.saveMedias(medias)

        // When/Then
        repository.flowMedias(showArtworkId).test {
            val result = awaitItem()
            assertEquals(expectedMedias.size, result.size)
            assertTrue(result.containsAll(expectedMedias))
        }
    }

    @Test
    fun flowMedias_emits_new_value_when_medias_are_modified_or_inserted() = runTest {
        // Given
        val showArtworkId = MediaMockups.showArtwork.id
        val episode1 = MediaMockups.episode1
        val episode2 = MediaMockups.episode2

        // When/Then
        repository.flowMedias(showArtworkId).test {
            assertEquals(0, awaitItem().size)

            repository.saveMedias(listOf(episode1))
            assertEquals(1, awaitItem().size)

            repository.saveMedias(listOf(episode2))
            assertEquals(2, awaitItem().size)
        }
    }

    @Test
    fun flowMedias_returns_empty_list_when_id_doesnt_exist() = runTest {
        // Given
        val incorrectId = 999L
        repository.saveMedias(MediaMockups.allMedias)

        // When/Then
        repository.flowMedias(incorrectId).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
        }
    }

    @Test
    fun flowSeasons_returns_flow_of_seasons_with_given_artwork_id() = runTest {
        // Given
        val seasons = MediaMockups.seasons
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedSeasons = seasons.filter { it.artworkId == showArtworkId }.sortedBy { it.season }
        repository.saveSeasons(seasons)

        // When/Then
        repository.flowSeasons(showArtworkId).test {
            val result = awaitItem()
            assertEquals(expectedSeasons, result)
        }
    }

    @Test
    fun flowMedias_emits_new_value_when_seasons_are_modified_or_inserted() = runTest {
        // Given
        val showArtworkId = MediaMockups.showArtwork.id
        val season1 = MediaMockups.season1
        val season2 = MediaMockups.season2

        // When/Then
        repository.flowSeasons(showArtworkId).test {
            assertEquals(0, awaitItem().size)

            repository.saveSeasons(listOf(season1))
            assertEquals(1, awaitItem().size)

            repository.saveSeasons(listOf(season2))
            assertEquals(2, awaitItem().size)
        }
    }

    @Test
    fun flowSeasons_returns_empty_list_when_id_doesnt_exist() = runTest {
        // Given
        val incorrectId = 999L
        repository.saveSeasons(MediaMockups.seasons)

        // When/Then
        repository.flowSeasons(incorrectId).test {
            val result = awaitItem()
            assertTrue(result.isEmpty())
        }
    }

    //endregion

    //region Save & Get

    @Test
    fun getArtworks_returns_artworks_inserted_by_saveArtworks() = runTest {
        // Given
        val artworks = MediaMockups.artworks

        // When
        repository.saveArtworks(artworks, overrideLastModification = false)

        // Then
        val result = repository.getArtworks()
        assertEquals(artworks.size, result.size)
        assertTrue(result.containsAll(artworks))
    }

    @Test
    fun getMedias_returns_medias_inserted_by_saveMedias() = runTest {
        // Given
        val medias = MediaMockups.allMedias

        // When
        repository.saveMedias(medias)

        // Then
        val result = repository.getMedias()
        assertEquals(medias.size, result.size)
        assertTrue(result.containsAll(medias))
    }

    @Test
    fun getSeasons_returns_seasons_inserted_by_saveSeasons() = runTest {
        // Given
        val seasons = MediaMockups.seasons

        // When
        repository.saveSeasons(seasons)

        // Then
        val result = repository.getSeasons()
        assertEquals(seasons.size, result.size)
        assertTrue(result.containsAll(seasons))
    }

    @Test
    fun getArtwork_returns_artwork_with_given_artworkId() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        repository.saveArtworks(listOf(artwork), overrideLastModification = false)

        // When
        val result = repository.getArtwork(artwork.id)

        // Then
        assertEquals(artwork, result)
    }

    @Test
    fun getArtwork_returns_null_when_given_artworkId_doesnt_exist() = runTest {
        // Given
        val incorrectId = 999L

        // When
        val result = repository.getArtwork(incorrectId)

        // Then
        assertNull(result)
    }

    @Test
    fun getMovie_returns_movie_with_given_artworkId() = runTest {
        // Given
        val movie = MediaMockups.movie
        repository.saveMedias(listOf(movie))

        // When
        val result = repository.getMovie(movie.artworkId)

        // Then
        assertEquals(movie, result)
    }

    @Test
    fun getMovie_returns_null_when_given_artworkId_doesnt_exist() = runTest {
        // Given
        val incorrectId = 999L

        // When
        val result = repository.getMovie(incorrectId)

        // Then
        assertNull(result)
    }

    @Test
    fun getEpisodes_returns_episodes_with_given_artworkId() = runTest {
        // Given
        val episodes = MediaMockups.episodes
        val showArtworkId = MediaMockups.showArtwork.id
        repository.saveMedias(episodes)

        // When
        val result = repository.getEpisodes(showArtworkId)

        // Then
        val expected = episodes.sort()
        assertEquals(expected, result)
    }

    @Test
    fun getEpisodes_returns_empty_list_when_given_artworkId_doesnt_exist() = runTest {
        // Given
        val incorrectId = 999L
        repository.saveMedias(MediaMockups.episodes)

        // When
        val result = repository.getEpisodes(incorrectId)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun getMediasNotInFiles_returns_medias_that_are_not_part_of_the_given_files() = runTest {
        // Given
        val medias = MediaMockups.allMedias
        repository.saveMedias(medias)
        val filesToKeep = listOf(MediaMockups.movie.file, MediaMockups.episode1.file)
        val expectedMedias = medias.filter { it.file !in filesToKeep }

        // When
        val result = repository.getMediasNotInFiles(filesToKeep)

        // Then
        assertEquals(expectedMedias.size, result.size)
        assertTrue(result.containsAll(expectedMedias))
    }

    @Test
    fun getSeasons_returns_seasons_with_given_artworkId() = runTest {
        // Given
        val seasons = MediaMockups.seasons
        val showArtworkId = MediaMockups.showArtwork.id
        val expectedSeasons = seasons.filter { it.artworkId == showArtworkId }.sortedBy { it.season }
        repository.saveSeasons(seasons)

        // When
        val result = repository.getSeasons(showArtworkId)

        // Then
        assertEquals(expectedSeasons, result)
    }

    @Test
    fun getSeasons_returns_empty_list_when_given_artworkId_doesnt_exist() = runTest {
        // Given
        val incorrectId = 999L
        repository.saveSeasons(MediaMockups.seasons)

        // When
        val result = repository.getSeasons(incorrectId)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun getUnknownMedias_returns_only_medias_with_unknown_id() = runTest {
        // Given
        val medias = MediaMockups.allMedias
        repository.saveMedias(medias)
        val expectedUnknowns = MediaMockups.unknowns

        // When
        val result = repository.getUnknownMedias()

        // Then
        assertEquals(expectedUnknowns.size, result.size)
        assertTrue(result.containsAll(expectedUnknowns))
    }

    //endregion

    //region Update

    @Test
    fun updateRealPaths_update_only_when_realPath_is_empty() = runTest {
        // Given
        val media1 = MediaMockups.movie.toEntity().copy(path = "path/1", realPath = "")
        val media2 = MediaMockups.movie2.toEntity().copy(path = "path/2", realPath = "/already/set/path")
        database.dao().insertMedias(listOf(media1, media2))

        val file1 = MediaMockups.movie.file.copy(path = "path/1", realPath = "/new/real/path/1")
        val file2 = MediaMockups.movie2.file.copy(path = "path/2", realPath = "/new/real/path/2")

        // When
        repository.updateRealPaths(listOf(file1, file2))

        // Then
        val mediasInDb = database.dao().getMedias()
        val dbMedia1 = mediasInDb.find { it.path == "path/1" }
        val dbMedia2 = mediasInDb.find { it.path == "path/2" }

        assertEquals("/new/real/path/1", dbMedia1?.realPath)
        assertEquals("/already/set/path", dbMedia2?.realPath)
    }

    //endregion

    //region Count

    @Test
    fun getEpisodeCount_returns_episodes_count_from_given_artwork_id() = runTest {
        // Given
        val medias = MediaMockups.allMedias
        repository.saveMedias(medias)
        val showArtworkId = MediaMockups.showArtwork.id

        // When
        val count = repository.getEpisodeCount(showArtworkId)

        // Then
        val expectedCount = medias.filterIsInstance<Episode>().count { it.artworkId == showArtworkId }
        assertEquals(expectedCount, count)
    }

    @Test
    fun getEpisodeCountBySeason_returns_episodes_count_from_given_artwork_id_and_season() = runTest {
        // Given
        val medias = MediaMockups.allMedias
        repository.saveMedias(medias)
        val showArtworkId = MediaMockups.showArtwork.id
        val seasonNumber = 1

        // When
        val count = repository.getEpisodeCountBySeason(showArtworkId, seasonNumber)

        // Then
        val expectedCount = medias.filterIsInstance<Episode>().count { it.artworkId == showArtworkId && it.season == seasonNumber }
        assertEquals(expectedCount, count)
    }

    //endregion

    //region Images

    @Test
    fun getAllImagesPaths_returns_all_image_paths_from_artworks_seasons_and_medias() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveMedias(MediaMockups.allMedias)
        repository.saveSeasons(MediaMockups.seasons)

        // When
        val result = repository.getAllImagesPaths()

        // Then
        val expectedArtworkImages = MediaMockups.artworks.flatMap { listOf(it.imagePath, it.bannerPath) }.filter { it.isNotBlank() }
        val expectedMediasImages = MediaMockups.allMedias.filterIsInstance<Episode>().map { it.imagePath }.filter { it.isNotBlank() }
        val expectedSeasonImages = MediaMockups.seasons.mapNotNull { it.imagePath }.filter { it.isNotBlank() }
        val expectedAll = expectedArtworkImages + expectedMediasImages + expectedSeasonImages

        assertEquals(expectedAll.size, result.size)
        assertTrue(result.containsAll(expectedAll))
    }

    //endregion

    //region Delete

    @Test
    fun deleteArtworks_deletes_given_artworks_then_deletes_related_seasons_and_medias() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveMedias(MediaMockups.allMedias)
        repository.saveSeasons(MediaMockups.seasons)
        val artworksToDelete = listOf(MediaMockups.showArtwork)

        // When
        repository.deleteArtworks(artworksToDelete)

        // Then
        val remainingArtworks = repository.getArtworks()
        val remainingMedias = repository.getMedias()
        val remainingSeasons = repository.getSeasons()

        assertTrue(remainingArtworks.none { it.id == MediaMockups.showArtwork.id })
        assertTrue(remainingMedias.none { it.artworkId == MediaMockups.showArtwork.id })
        assertTrue(remainingSeasons.none { it.artworkId == MediaMockups.showArtwork.id })
    }

    @Test
    fun deleteMedias_deletes_given_medias_then_delete_empty_seasons_and_artworks() = runTest {
        // Given
        repository.saveArtworks(listOf(MediaMockups.movieArtwork))
        repository.saveMedias(listOf(MediaMockups.movie))

        // When
        repository.deleteMedias(listOf(MediaMockups.movie))

        // Then
        assertTrue(repository.getMedias().isEmpty())
        assertTrue(repository.getArtworks().isEmpty())
    }

    @Test
    fun deleteMediasNotInFiles_deletes_media_that_are_not_part_of_the_given_files_then_delete_empty_seasons_and_artworks() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks, overrideLastModification = false)
        repository.saveMedias(MediaMockups.allMedias)
        repository.saveSeasons(MediaMockups.seasons)
        val existingFiles = listOf(MediaMockups.movie.file)

        // When
        repository.deleteMediasNotInFiles(existingFiles)

        // Then
        val remainingMedias = repository.getMedias()
        val remainingArtworks = repository.getArtworks()

        assertEquals(1, remainingMedias.size)
        assertEquals(MediaMockups.movie, remainingMedias.first())
        assertEquals(1, remainingArtworks.size)
        assertEquals(MediaMockups.movieArtwork, remainingArtworks.first())
    }

    @Test
    fun deleteMediasInFolder_deletes_all_medias_within_given_folder_then_delete_empty_seasons_and_artworks() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks, overrideLastModification = false)
        repository.saveMedias(MediaMockups.allMedias)
        repository.saveSeasons(MediaMockups.seasons)
        val folder = UserFolder(path = "path/naruto")

        // When
        repository.deleteMediasInFolder(folder)

        // Then
        val remainingMedias = repository.getMedias()
        val remainingArtworks = repository.getArtworks()
        val remainingSeasons = repository.getSeasons()

        assertTrue(remainingMedias.none { it.file.path.startsWith(folder.path) })
        assertTrue(remainingArtworks.none { it.id == MediaMockups.showArtwork.id })
        assertTrue(remainingSeasons.none { it.artworkId == MediaMockups.showArtwork.id })
    }

    @Test
    fun deleteAll_deletes_all_artworks_seasons_and_medias() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveMedias(MediaMockups.allMedias)
        repository.saveSeasons(MediaMockups.seasons)

        // When
        repository.deleteAll()

        // Then
        assertTrue(repository.getArtworks().isEmpty())
        assertTrue(repository.getMedias().isEmpty())
        assertTrue(repository.getSeasons().isEmpty())
    }

    //endregion
}