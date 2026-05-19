package com.mskd.flux.data.ddb

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Season
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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

    // region Insert & Get Artworks

    @Test
    fun insertArtworks_and_getArtworks_returns_all_inserted() = runTest {
        // Given
        val artworks = MediaMockups.artworks

        // When
        dao.insertArtworks(artworks)

        // Then
        val result = dao.getArtworks()
        assertEquals(artworks.size, result.size)
        assertTrue(result.containsAll(artworks))
    }

    @Test
    fun getArtwork_returns_correct_artwork_by_id() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        dao.insertArtworks(listOf(artwork))

        // When
        val result = dao.getArtwork(artwork.id)

        // Then
        assertNotNull(result)
        assertEquals(artwork, result)
    }

    @Test
    fun getArtwork_returns_null_when_not_found() = runTest {
        // When
        val result = dao.getArtwork(999999L)

        // Then
        assertNull(result)
    }

    @Test
    fun insertArtworks_replaces_on_conflict() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val updatedArtwork = artwork.copy(title = "Updated Title")
        dao.insertArtworks(listOf(artwork))

        // When
        dao.insertArtworks(listOf(updatedArtwork))

        // Then
        val result = dao.getArtwork(artwork.id)
        assertEquals("Updated Title", result?.title)
        assertEquals(1, dao.getArtworks().size)
    }

    // endregion

    // region Insert & Get Movies

    @Test
    fun insertMovies_and_getMovie_returns_correct_movie() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        dao.insertArtworks(listOf(artwork))

        // When
        dao.insertMovies(listOf(movie))

        // Then
        val result = dao.getMovie(artwork.id)
        assertNotNull(result)
        assertEquals(movie, result)
    }

    @Test
    fun getMovie_returns_null_when_not_found() = runTest {
        // When
        val result = dao.getMovie(999999L)

        // Then
        assertNull(result)
    }

    @Test
    fun getMovies_returns_all_movies() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        dao.insertArtworks(listOf(artwork))
        dao.insertMovies(listOf(movie))

        // When
        val result = dao.getMovies()

        // Then
        assertEquals(1, result.size)
        assertEquals(movie, result.first())
    }

    @Test
    fun getMoviesNotInFiles_returns_movies_whose_filename_is_not_in_list() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        dao.insertArtworks(listOf(artwork))
        dao.insertMovies(listOf(movie))

        // When - file name matches, so no movie should be returned
        val resultEmpty = dao.getMoviesNotInFiles(listOf(movie.file.name))

        // Then
        assertTrue(resultEmpty.isEmpty())

        // When - file name does NOT match, so the movie should be returned
        val resultWithMovie = dao.getMoviesNotInFiles(listOf("other_file.mkv"))

        // Then
        assertEquals(1, resultWithMovie.size)
        assertEquals(movie, resultWithMovie.first())
    }

    @Test
    fun insertMovies_replaces_on_conflict() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        dao.insertArtworks(listOf(artwork))
        dao.insertMovies(listOf(movie))

        // When
        val updatedMovie = movie.copy(title = "Updated Movie Title")
        dao.insertMovies(listOf(updatedMovie))

        // Then
        val result = dao.getMovie(artwork.id)
        assertEquals("Updated Movie Title", result?.title)
        assertEquals(1, dao.getMovies().size)
    }

    // endregion

    // region Insert & Get Episodes

    @Test
    fun insertEpisodes_and_getEpisodes_by_artworkId() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        dao.insertArtworks(listOf(artwork))

        // When
        dao.insertEpisodes(episodes)

        // Then
        val result = dao.getEpisodes(artwork.id)
        assertEquals(2, result.size)
        assertEquals(episodes, result)
    }

    @Test
    fun getEpisodes_without_artworkId_returns_all_episodes() = runTest {
        // Given
        val artworks = listOf(MediaMockups.showArtwork, MediaMockups.unknownArtwork)
        val allEpisodes = MediaMockups.episodes + MediaMockups.unknowns
        dao.insertArtworks(artworks)
        dao.insertEpisodes(allEpisodes)

        // When
        val result = dao.getEpisodes()

        // Then
        assertEquals(allEpisodes.size, result.size)
    }

    @Test
    fun getEpisodesNotInFiles_returns_episodes_whose_filename_is_not_in_list() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(episodes)

        // When - exclude episode1's file name
        val result = dao.getEpisodesNotInFiles(listOf(MediaMockups.episode1.file.name))

        // Then - only episode2 should remain
        assertEquals(1, result.size)
        assertEquals(MediaMockups.episode2, result.first())
    }

    @Test
    fun getUnknownMedias_returns_only_episodes_with_unknown_artworkId() = runTest {
        // Given
        val artworks = listOf(MediaMockups.showArtwork, MediaMockups.unknownArtwork)
        val knownEpisodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        val unknownEpisodes = MediaMockups.unknowns
        dao.insertArtworks(artworks)
        dao.insertEpisodes(knownEpisodes + unknownEpisodes)

        // When
        val result = dao.getUnknownMedias()

        // Then
        assertEquals(unknownEpisodes.size, result.size)
        result.forEach { episode -> assertEquals(Artwork.UNKNOWN_ID, episode.artworkId) }
    }

    // endregion

    // region Insert & Get Seasons

    @Test
    fun insertSeasons_and_getSeasons_by_artworkId() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val seasons = MediaMockups.seasons
        dao.insertArtworks(listOf(artwork))

        // When
        dao.insertSeasons(seasons)

        // Then
        val result = dao.getSeasons(artwork.id)
        assertEquals(2, result.size)
        assertEquals(seasons, result)
    }

    @Test
    fun getSeasons_without_artworkId_returns_all_seasons() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(MediaMockups.seasons)

        // When
        val result = dao.getSeasons()

        // Then
        assertEquals(MediaMockups.seasons.size, result.size)
    }

    @Test
    fun insertSeasons_replaces_on_conflict() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val season = MediaMockups.season1
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(listOf(season))

        // When
        val updatedSeason = season.copy(title = "Updated Season Title")
        dao.insertSeasons(listOf(updatedSeason))

        // Then
        val result = dao.getSeasons(artwork.id)
        assertEquals(1, result.size)
        assertEquals("Updated Season Title", result.first().title)
    }

    // endregion

    // region Delete

    @Test
    fun deleteArtworks_removes_artworks_by_ids() = runTest {
        // Given
        val artworks = MediaMockups.artworks
        dao.insertArtworks(artworks)

        // When
        dao.deleteArtworks(listOf(MediaMockups.movieArtwork.id))

        // Then
        assertNull(dao.getArtwork(MediaMockups.movieArtwork.id))
        assertNotNull(dao.getArtwork(MediaMockups.showArtwork.id))
    }

    @Test
    fun deleteMoviesByIds_removes_movies_by_artworkIds() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        dao.insertArtworks(listOf(artwork))
        dao.insertMovies(listOf(movie))

        // When
        dao.deleteMoviesByIds(listOf(artwork.id))

        // Then
        assertNull(dao.getMovie(artwork.id))
    }

    @Test
    fun deleteEpisodesByIds_removes_episodes_by_ids() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(episodes)

        // When
        dao.deleteEpisodesByIds(listOf(MediaMockups.episode1.id))

        // Then
        assert(dao.getEpisodes().none { it.id ==  MediaMockups.episode1.id})
        assert(dao.getEpisodes().any { it.id ==  MediaMockups.episode2.id})
    }

    @Test
    fun deleteEpisodesByArtworkId_removes_all_episodes_of_an_artwork() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2, MediaMockups.episode3)
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(episodes)

        // When
        dao.deleteEpisodesByArtworkId(artwork.id)

        // Then
        val result = dao.getEpisodes(artwork.id)
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteAllArtworks_clears_artworks_table() = runTest {
        // Given
        dao.insertArtworks(MediaMockups.artworks)

        // When
        dao.deleteAllArtworks()

        // Then
        assertTrue(dao.getArtworks().isEmpty())
    }

    @Test
    fun deleteAllMovies_clears_movies_table() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertMovies(listOf(MediaMockups.movie))

        // When
        dao.deleteAllMovies()

        // Then
        assertTrue(dao.getMovies().isEmpty())
    }

    @Test
    fun deleteAllEpisodes_clears_episodes_table() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(listOf(MediaMockups.episode1, MediaMockups.episode2))

        // When
        dao.deleteAllEpisodes()

        // Then
        assertTrue(dao.getEpisodes().isEmpty())
    }

    @Test
    fun delete_artwork_does_not_affect_movies() = runTest {
        // Given - No ForeignKey constraint
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        dao.insertArtworks(listOf(artwork))
        dao.insertMovies(listOf(movie))

        // When
        dao.deleteArtworks(listOf(artwork.id))

        // Then - artwork is deleted but movie remains (no FK)
        assertNull(dao.getArtwork(artwork.id))
        assertNotNull(dao.getMovie(artwork.id))
    }

    @Test
    fun delete_artwork_does_not_affect_episodes() = runTest {
        // Given - No ForeignKey constraint
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(episodes)

        // When
        dao.deleteArtworks(listOf(artwork.id))

        // Then - artwork is deleted but episodes remain (no FK)
        assertNull(dao.getArtwork(artwork.id))
        assertEquals(2, dao.getEpisodes(artwork.id).size)
    }

    @Test
    fun deleteSeasonsByIds_removes_seasons_by_artworkIds() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(MediaMockups.seasons)

        // When
        dao.deleteSeasonsByIds(listOf(artwork.id))

        // Then
        assertTrue(dao.getSeasons(artwork.id).isEmpty())
    }

    @Test
    fun deleteSeason_removes_specific_season() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(MediaMockups.seasons)

        // When
        dao.deleteSeason(artworkId = artwork.id, season = 1)

        // Then
        val result = dao.getSeasons(artwork.id)
        assertEquals(1, result.size)
        assertEquals(MediaMockups.season2, result.first())
    }

    @Test
    fun deleteEmptySeasons_removes_seasons_without_matching_episodes() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(MediaMockups.seasons)
        // Insert episodes only for season 1
        dao.insertEpisodes(listOf(MediaMockups.episode1, MediaMockups.episode2))

        // When
        dao.deleteEmptySeasons()

        // Then - season2 has no episodes, should be deleted
        val result = dao.getSeasons(artwork.id)
        assertEquals(1, result.size)
        assertEquals(MediaMockups.season1, result.first())
    }

    @Test
    fun deleteEmptySeasons_keeps_all_seasons_when_all_have_episodes() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(MediaMockups.seasons)
        // Insert episodes for both seasons
        dao.insertEpisodes(MediaMockups.episodes)

        // When
        dao.deleteEmptySeasons()

        // Then - all seasons have episodes, none deleted
        val result = dao.getSeasons(artwork.id)
        assertEquals(2, result.size)
    }

    @Test
    fun deleteAllSeasons_clears_seasons_table() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(MediaMockups.seasons)

        // When
        dao.deleteAllSeasons()

        // Then
        assertTrue(dao.getSeasons().isEmpty())
    }

    // endregion

    // region Count

    @Test
    fun getEpisodeCountByArtworkId_returns_correct_count() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2, MediaMockups.episode3)
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(episodes)

        // When
        val count = dao.getEpisodeCountByArtworkId(artwork.id)

        // Then
        assertEquals(3, count)
    }

    @Test
    fun getEpisodeCountByArtworkId_returns_zero_when_no_episodes() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))

        // When
        val count = dao.getEpisodeCountByArtworkId(artwork.id)

        // Then
        assertEquals(0, count)
    }

    @Test
    fun getEpisodeCountBySeason_returns_correct_count() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2, MediaMockups.episode3)
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(episodes)

        // When
        val countSeason1 = dao.getEpisodeCountBySeason(artwork.id, season = 1)
        val countSeason2 = dao.getEpisodeCountBySeason(artwork.id, season = 2)

        // Then
        assertEquals(2, countSeason1) // episode1, episode2
        assertEquals(1, countSeason2) // episode3
    }

    @Test
    fun getEpisodeCountBySeason_returns_zero_when_no_episodes_for_season() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertEpisodes(listOf(MediaMockups.episode1)) // Only season 1 episode

        // When
        val count = dao.getEpisodeCountBySeason(artwork.id, season = 99)

        // Then
        assertEquals(0, count)
    }

    // endregion

    // region Images

    @Test
    fun getArtworksImages_returns_image_and_banner_paths() = runTest {
        // Given
        dao.insertArtworks(MediaMockups.artworks)

        // When
        val result = dao.getArtworksImages()

        // Then
        assertEquals(MediaMockups.artworks.size, result.size)
        val expectedPairs = MediaMockups.artworks.map { it.imagePath to it.bannerPath }
        val actualPairs = result.map { it.imagePath to it.bannerPath }
        assertTrue(actualPairs.containsAll(expectedPairs))
    }

    @Test
    fun getEpisodesImages_returns_all_episode_image_paths() = runTest {
        // Given
        val artworks = listOf(MediaMockups.showArtwork, MediaMockups.unknownArtwork)
        val episodes = MediaMockups.episodes + MediaMockups.unknowns
        dao.insertArtworks(artworks)
        dao.insertEpisodes(episodes)

        // When
        val result = dao.getEpisodesImages()

        // Then
        assertEquals(episodes.size, result.size)
        val expectedPaths = episodes.map { it.imagePath }
        assertTrue(result.containsAll(expectedPaths))
    }

    @Test
    fun getSeasonsImages_returns_all_season_image_paths() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        dao.insertArtworks(listOf(artwork))
        dao.insertSeasons(MediaMockups.seasons)

        // When
        val result = dao.getSeasonsImages()

        // Then
        assertEquals(MediaMockups.seasons.size, result.size)
        val expectedPaths = MediaMockups.seasons.map { it.imagePath }
        assertTrue(result.containsAll(expectedPaths))
    }

    // endregion

    // region Flows

    @Test
    fun flowArtworks_emits_updates_when_artworks_change() = runTest {
        dao.flowArtworks().test {
            // Initial emission - empty
            assertEquals(emptyList<Artwork>(), awaitItem())

            // Insert artworks
            dao.insertArtworks(listOf(MediaMockups.movieArtwork))
            val afterInsert = awaitItem()
            assertEquals(1, afterInsert.size)
            assertEquals(MediaMockups.movieArtwork, afterInsert.first())

            // Insert another artwork
            dao.insertArtworks(listOf(MediaMockups.showArtwork))
            val afterSecondInsert = awaitItem()
            assertEquals(2, afterSecondInsert.size)

            // Delete an artwork
            dao.deleteArtworks(listOf(MediaMockups.movieArtwork.id))
            val afterDelete = awaitItem()
            assertEquals(1, afterDelete.size)
            assertEquals(MediaMockups.showArtwork, afterDelete.first())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowArtwork_emits_null_when_not_found() = runTest {
        dao.flowArtwork(999999L).test {
            // Initial emission - null
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowArtwork_emits_updates_for_specific_artwork() = runTest {
        val artwork = MediaMockups.movieArtwork

        dao.flowArtwork(artwork.id).test {
            // Initial emission - null
            assertNull(awaitItem())

            // Insert artwork
            dao.insertArtworks(listOf(artwork))
            val afterInsert = awaitItem()
            assertNotNull(afterInsert)
            assertEquals(artwork, afterInsert)

            // Update artwork
            val updated = artwork.copy(title = "Updated")
            dao.insertArtworks(listOf(updated))
            val afterUpdate = awaitItem()
            assertEquals("Updated", afterUpdate?.title)

            // Delete artwork
            dao.deleteArtworks(listOf(artwork.id))
            assertNull(awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowMovie_emits_updates_for_specific_movie() = runTest {
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        dao.insertArtworks(listOf(artwork))

        dao.flowMovie(artwork.id).test {
            // Initial emission - null
            assertNull(awaitItem())

            // Insert movie
            dao.insertMovies(listOf(movie))
            val afterInsert = awaitItem()
            assertNotNull(afterInsert)
            assertEquals(movie, afterInsert)

            // Delete movie
            dao.deleteMoviesByIds(listOf(artwork.id))
            assertNull(awaitItem())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowEpisodes_emits_updates_for_specific_artwork() = runTest {
        val artwork = MediaMockups.showArtwork
        val episode1 = MediaMockups.episode1
        val episode2 = MediaMockups.episode2
        dao.insertArtworks(listOf(artwork))

        dao.flowEpisodes(artwork.id).test {
            // Initial emission - empty list
            assertEquals(emptyList<Any>(), awaitItem())

            // Insert episodes
            dao.insertEpisodes(listOf(episode1))
            val afterFirst = awaitItem()
            assertEquals(1, afterFirst.size)

            dao.insertEpisodes(listOf(episode2))
            val afterSecond = awaitItem()
            assertEquals(2, afterSecond.size)

            // Delete one episode
            dao.deleteEpisodesByIds(listOf(episode1.id))
            val afterDelete = awaitItem()
            assertEquals(1, afterDelete.size)
            assertEquals(episode2, afterDelete.first())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowSeasons_emits_updates_for_specific_artwork() = runTest {
        val artwork = MediaMockups.showArtwork
        val season1 = MediaMockups.season1
        val season2 = MediaMockups.season2
        dao.insertArtworks(listOf(artwork))

        dao.flowSeasons(artwork.id).test {
            // Initial emission - empty list
            assertEquals(emptyList<Season>(), awaitItem())

            // Insert season1
            dao.insertSeasons(listOf(season1))
            val afterFirst = awaitItem()
            assertEquals(1, afterFirst.size)
            assertEquals(season1, afterFirst.first())

            // Insert season2
            dao.insertSeasons(listOf(season2))
            val afterSecond = awaitItem()
            assertEquals(2, afterSecond.size)

            // Delete season1
            dao.deleteSeason(artworkId = artwork.id, season = 1)
            val afterDelete = awaitItem()
            assertEquals(1, afterDelete.size)
            assertEquals(season2, afterDelete.first())

            cancelAndConsumeRemainingEvents()
        }
    }

    // endregion

    // region Edge Cases

    @Test
    fun delete_with_empty_id_list_does_not_affect_data() = runTest {
        // Given
        dao.insertArtworks(MediaMockups.artworks)

        // When
        dao.deleteArtworks(emptyList())

        // Then
        assertEquals(MediaMockups.artworks.size, dao.getArtworks().size)
    }

    @Test
    fun getEpisodes_for_nonexistent_artworkId_returns_empty_list() = runTest {
        // When
        val result = dao.getEpisodes(999999L)

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteAllArtworks_and_deleteAllMovies_and_deleteAllEpisodes_and_deleteAllSeasons_clears_everything() = runTest {
        // Given
        dao.insertArtworks(MediaMockups.artworks)
        dao.insertMovies(MediaMockups.movies)
        dao.insertEpisodes(MediaMockups.episodes + MediaMockups.unknowns)
        dao.insertSeasons(MediaMockups.seasons)

        // When
        dao.deleteAllSeasons()
        dao.deleteAllEpisodes()
        dao.deleteAllMovies()
        dao.deleteAllArtworks()

        // Then
        assertTrue(dao.getArtworks().isEmpty())
        assertTrue(dao.getMovies().isEmpty())
        assertTrue(dao.getEpisodes().isEmpty())
        assertTrue(dao.getSeasons().isEmpty())
    }

    @Test
    fun getSeasons_for_nonexistent_artworkId_returns_empty_list() = runTest {
        // When
        val result = dao.getSeasons(999999L)

        // Then
        assertTrue(result.isEmpty())
    }

    // endregion

}
