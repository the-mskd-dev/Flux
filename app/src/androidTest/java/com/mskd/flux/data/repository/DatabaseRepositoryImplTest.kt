package com.mskd.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.data.ddb.FluxDatabase
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
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

        repository = DatabaseRepositoryImpl(database.dao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    // region Save & Get Artworks

    @Test
    fun saveArtworks_and_getArtworks_returns_all_inserted() = runTest {
        // Given
        val artworks = MediaMockups.artworks

        // When
        repository.saveArtworks(artworks)

        // Then
        val result = repository.getArtworks()
        Assert.assertEquals(artworks.size, result.size)
        Assert.assertTrue(result.containsAll(artworks))
    }

    @Test
    fun getArtwork_returns_correct_artwork_by_id() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        repository.saveArtworks(listOf(artwork))

        // When
        val result = repository.getArtwork(artwork.id)

        // Then
        Assert.assertNotNull(result)
        Assert.assertEquals(artwork, result)
    }

    @Test
    fun getArtwork_returns_null_when_not_found() = runTest {
        // When
        val result = repository.getArtwork(999999L)

        // Then
        Assert.assertNull(result)
    }

    // endregion

    // region Save & Get Movies

    @Test
    fun saveMovies_and_getMovie_returns_correct_movie() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        repository.saveArtworks(listOf(artwork))

        // When
        repository.saveMovies(listOf(movie))

        // Then
        val result = repository.getMovie(artwork.id)
        Assert.assertNotNull(result)
        Assert.assertEquals(movie, result)
    }

    @Test
    fun getMovie_returns_null_when_not_found() = runTest {
        // When
        val result = repository.getMovie(999999L)

        // Then
        Assert.assertNull(result)
    }

    @Test
    fun getMovies_returns_all_movies() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        repository.saveArtworks(listOf(artwork))
        repository.saveMovies(listOf(movie))

        // When
        val result = repository.getMovies()

        // Then
        Assert.assertEquals(1, result.size)
        Assert.assertEquals(movie, result.first())
    }

    @Test
    fun getMoviesNotInFiles_returns_movies_whose_file_is_not_in_list() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        repository.saveArtworks(listOf(artwork))
        repository.saveMovies(listOf(movie))

        // When - file matches, so no movie should be returned
        val resultEmpty = repository.getMoviesNotInFiles(listOf(movie.file))

        // Then
        Assert.assertTrue(resultEmpty.isEmpty())

        // When - file does NOT match, so the movie should be returned
        val resultWithMovie = repository.getMoviesNotInFiles(listOf(MediaMockups.episode1.file))

        // Then
        Assert.assertEquals(1, resultWithMovie.size)
        Assert.assertEquals(movie, resultWithMovie.first())
    }

    // endregion

    // region Save & Get Episodes

    @Test
    fun saveEpisodes_and_getEpisodes_by_artworkId() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        repository.saveArtworks(listOf(artwork))

        // When
        repository.saveEpisodes(episodes)

        // Then
        val result = repository.getEpisodes(artwork.id)
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(episodes, result)
    }

    @Test
    fun getEpisodes_without_artworkId_returns_all_episodes() = runTest {
        // Given
        val artworks = listOf(MediaMockups.showArtwork, MediaMockups.unknownArtwork)
        val allEpisodes = MediaMockups.episodes + MediaMockups.unknowns
        repository.saveArtworks(artworks)
        repository.saveEpisodes(allEpisodes)

        // When
        val result = repository.getEpisodes()

        // Then
        Assert.assertEquals(allEpisodes.size, result.size)
    }

    @Test
    fun getEpisodesNotInFiles_returns_episodes_whose_file_is_not_in_list() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(episodes)

        // When - exclude episode1's file
        val result = repository.getEpisodesNotInFiles(listOf(MediaMockups.episode1.file))

        // Then - only episode2 should remain
        Assert.assertEquals(1, result.size)
        Assert.assertEquals(MediaMockups.episode2, result.first())
    }

    @Test
    fun getUnknownMedias_returns_only_episodes_with_unknown_artworkId() = runTest {
        // Given
        val artworks = listOf(MediaMockups.showArtwork, MediaMockups.unknownArtwork)
        val knownEpisodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        val unknownEpisodes = MediaMockups.unknowns
        repository.saveArtworks(artworks)
        repository.saveEpisodes(knownEpisodes + unknownEpisodes)

        // When
        val result = repository.getUnknownMedias()

        // Then
        Assert.assertEquals(unknownEpisodes.size, result.size)
        result.forEach { episode ->
            Assert.assertEquals(
                Artwork.Companion.UNKNOWN_ID,
                episode.artworkId
            )
        }
    }

    // endregion

    // region Save & Get Seasons

    @Test
    fun saveSeasons_and_getSeasons_by_artworkId() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))

        // When
        repository.saveSeasons(MediaMockups.seasons)

        // Then
        val result = repository.getSeasons(artwork.id)
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(MediaMockups.seasons, result)
    }

    @Test
    fun getSeasons_without_artworkId_returns_all_seasons() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveSeasons(MediaMockups.seasons)

        // When
        val result = repository.getSeasons()

        // Then
        Assert.assertEquals(MediaMockups.seasons.size, result.size)
    }

    // endregion

    // region Count

    @Test
    fun getEpisodeCount_returns_correct_count() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(MediaMockups.episodes)

        // When
        val count = repository.getEpisodeCount(artwork.id)

        // Then
        Assert.assertEquals(3, count)
    }

    @Test
    fun getEpisodeCount_returns_zero_when_no_episodes() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))

        // When
        val count = repository.getEpisodeCount(artwork.id)

        // Then
        Assert.assertEquals(0, count)
    }

    @Test
    fun getEpisodeCountBySeason_returns_correct_count() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(MediaMockups.episodes)

        // When
        val countSeason1 = repository.getEpisodeCountBySeason(artwork.id, season = 1)
        val countSeason2 = repository.getEpisodeCountBySeason(artwork.id, season = 2)

        // Then
        Assert.assertEquals(2, countSeason1) // episode1, episode2
        Assert.assertEquals(1, countSeason2) // episode3
    }

    @Test
    fun getEpisodeCountBySeason_returns_zero_when_no_episodes_for_season() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(listOf(MediaMockups.episode1))

        // When
        val count = repository.getEpisodeCountBySeason(artwork.id, season = 99)

        // Then
        Assert.assertEquals(0, count)
    }

    // endregion

    // region Images

    @Test
    fun getAllImagesPaths_returns_all_image_paths_with_tmdb_prefix() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveEpisodes(MediaMockups.episodes)
        repository.saveSeasons(MediaMockups.seasons)

        // When
        val result = repository.getAllImagesPaths()

        // Then - should contain artwork images, artwork banners, episode images, season images
        // Artworks with non-blank imagePath: movieArtwork, showArtwork (unknownArtwork has blank paths)
        val expectedArtworkImages = MediaMockups.artworks
            .filter { it.imagePath.isNotBlank() }
            .map { it.imagePath.tmdbImage }
        val expectedArtworkBanners = MediaMockups.artworks
            .filter { it.imagePath.isNotBlank() }
            .map { it.bannerPath.tmdbImageLarge }
        val expectedEpisodeImages = MediaMockups.episodes
            .filter { it.imagePath.isNotBlank() }
            .map { it.imagePath.tmdbImage }
        val expectedSeasonImages = MediaMockups.seasons
            .filter { it.imagePath?.isNotBlank() == true }
            .map { it.imagePath!!.tmdbImage }

        Assert.assertTrue(result.containsAll(expectedArtworkImages))
        Assert.assertTrue(result.containsAll(expectedArtworkBanners))
        Assert.assertTrue(result.containsAll(expectedEpisodeImages))
        Assert.assertTrue(result.containsAll(expectedSeasonImages))
    }

    @Test
    fun getAllImagesPaths_returns_empty_when_no_data() = runTest {
        // When
        val result = repository.getAllImagesPaths()

        // Then
        Assert.assertTrue(result.isEmpty())
    }

    // endregion

    // region Delete Artworks

    @Test
    fun deleteArtworks_removes_artworks_movies_episodes_and_seasons() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(MediaMockups.episodes)
        repository.saveSeasons(MediaMockups.seasons)

        // When
        repository.deleteArtworks(listOf(artwork))

        // Then - artwork, its episodes, and its seasons should all be deleted
        Assert.assertNull(repository.getArtwork(artwork.id))
        Assert.assertTrue(repository.getEpisodes(artwork.id).isEmpty())
        Assert.assertTrue(repository.getSeasons(artwork.id).isEmpty())
    }

    @Test
    fun deleteArtworks_removes_artwork_and_movie() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        repository.saveArtworks(listOf(artwork))
        repository.saveMovies(listOf(movie))

        // When
        repository.deleteArtworks(listOf(artwork))

        // Then
        Assert.assertNull(repository.getArtwork(artwork.id))
        Assert.assertNull(repository.getMovie(artwork.id))
    }

    @Test
    fun deleteArtworks_does_not_affect_other_artworks() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveMovies(MediaMockups.movies)
        repository.saveEpisodes(MediaMockups.episodes)
        repository.saveSeasons(MediaMockups.seasons)

        // When - delete only movieArtwork
        repository.deleteArtworks(listOf(MediaMockups.movieArtwork))

        // Then - showArtwork and its data should remain
        Assert.assertNull(repository.getArtwork(MediaMockups.movieArtwork.id))
        Assert.assertNull(repository.getMovie(MediaMockups.movieArtwork.id))
        Assert.assertNotNull(repository.getArtwork(MediaMockups.showArtwork.id))
        Assert.assertEquals(3, repository.getEpisodes(MediaMockups.showArtwork.id).size)
        Assert.assertEquals(2, repository.getSeasons(MediaMockups.showArtwork.id).size)
    }

    // endregion

    // region Delete Movies

    @Test
    fun deleteMovies_removes_movies_and_related_artworks() = runTest {
        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        repository.saveArtworks(listOf(artwork))
        repository.saveMovies(listOf(movie))

        // When
        repository.deleteMovies(listOf(movie))

        // Then - both movie and artwork should be deleted
        Assert.assertNull(repository.getMovie(artwork.id))
        Assert.assertNull(repository.getArtwork(artwork.id))
    }

    @Test
    fun deleteMovies_does_not_affect_other_artworks() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveMovies(MediaMockups.movies)
        repository.saveEpisodes(MediaMockups.episodes)

        // When
        repository.deleteMovies(MediaMockups.movies)

        // Then - show artwork and episodes should remain
        Assert.assertNotNull(repository.getArtwork(MediaMockups.showArtwork.id))
        Assert.assertEquals(3, repository.getEpisodes(MediaMockups.showArtwork.id).size)
    }

    // endregion

    // region Delete Episodes

    @Test
    fun deleteEpisodes_removes_episodes_and_empty_seasons_and_empty_artworks() = runTest {
        // Given - artwork with episodes and seasons
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(MediaMockups.episodes)
        repository.saveSeasons(MediaMockups.seasons)

        // When - delete all episodes
        repository.deleteEpisodes(MediaMockups.episodes)

        // Then - episodes are deleted, seasons without episodes are deleted, artwork without episodes is deleted
        Assert.assertTrue(repository.getEpisodes(artwork.id).isEmpty())
        Assert.assertTrue(repository.getSeasons(artwork.id).isEmpty())
        Assert.assertNull(repository.getArtwork(artwork.id))
    }

    @Test
    fun deleteEpisodes_keeps_seasons_and_artworks_with_remaining_episodes() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(MediaMockups.episodes)
        repository.saveSeasons(MediaMockups.seasons)

        // When - delete only season 2 episodes (episode3)
        repository.deleteEpisodes(listOf(MediaMockups.episode3))

        // Then - season 1 episodes remain, season1 remains, artwork remains
        Assert.assertEquals(2, repository.getEpisodes(artwork.id).size)
        Assert.assertNotNull(repository.getArtwork(artwork.id))

        // Season2 has no episodes, should be deleted; season1 should remain
        val remainingSeasons = repository.getSeasons(artwork.id)
        Assert.assertEquals(1, remainingSeasons.size)
        Assert.assertEquals(MediaMockups.season1, remainingSeasons.first())
    }

    // endregion

    // region Delete Medias Not In Files

    @Test
    fun deleteMediasNotInFiles_removes_movies_not_in_files() = runTest {
        // Given
        repository.saveArtworks(listOf(MediaMockups.movieArtwork))
        repository.saveMovies(listOf(MediaMockups.movie))

        // When - pass empty file list, so all movies should be deleted
        repository.deleteMediasNotInFiles(emptyList())

        // Then
        Assert.assertTrue(repository.getMovies().isEmpty())
        Assert.assertNull(repository.getArtwork(MediaMockups.movieArtwork.id))
    }

    @Test
    fun deleteMediasNotInFiles_removes_episodes_not_in_files() = runTest {
        // Given
        val artwork = MediaMockups.showArtwork
        repository.saveArtworks(listOf(artwork))
        repository.saveEpisodes(MediaMockups.episodes)
        repository.saveSeasons(MediaMockups.seasons)

        // When - pass only episode1's file, so episode2 and episode3 should be deleted
        repository.deleteMediasNotInFiles(listOf(MediaMockups.episode1.file))

        // Then - only episode1 should remain
        val remaining = repository.getEpisodes(artwork.id)
        Assert.assertEquals(1, remaining.size)
        Assert.assertEquals(MediaMockups.episode1, remaining.first())
    }

    @Test
    fun deleteMediasNotInFiles_keeps_all_when_all_files_present() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveMovies(MediaMockups.movies)
        repository.saveEpisodes(MediaMockups.episodes)

        val allFiles = MediaMockups.movies.map { it.file } + MediaMockups.episodes.map { it.file }

        // When
        repository.deleteMediasNotInFiles(allFiles)

        // Then
        Assert.assertEquals(1, repository.getMovies().size)
        Assert.assertEquals(3, repository.getEpisodes().size)
    }

    // endregion

    // region Delete All

    @Test
    fun deleteAll_clears_all_tables() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)
        repository.saveMovies(MediaMockups.movies)
        repository.saveEpisodes(MediaMockups.episodes + MediaMockups.unknowns)
        repository.saveSeasons(MediaMockups.seasons)

        // When
        repository.deleteAll()

        // Then
        Assert.assertTrue(repository.getArtworks().isEmpty())
        Assert.assertTrue(repository.getMovies().isEmpty())
        Assert.assertTrue(repository.getEpisodes().isEmpty())
        Assert.assertTrue(repository.getSeasons().isEmpty())
    }

    // endregion

    // region Flows

    @Test
    fun flowArtworks_emits_updates_when_artworks_change() = runTest {
        repository.flowArtworks().test {
            // Initial emission - empty
            Assert.assertEquals(emptyList<Artwork>(), awaitItem())

            // Insert artworks
            repository.saveArtworks(listOf(MediaMockups.movieArtwork))
            val afterInsert = awaitItem()
            Assert.assertEquals(1, afterInsert.size)
            Assert.assertEquals(MediaMockups.movieArtwork, afterInsert.first())

            // Insert another artwork
            repository.saveArtworks(listOf(MediaMockups.showArtwork))
            val afterSecondInsert = awaitItem()
            Assert.assertEquals(2, afterSecondInsert.size)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowArtwork_emits_updates_for_specific_artwork() = runTest {
        val artwork = MediaMockups.movieArtwork

        repository.flowArtwork(artwork.id).test {
            // Initial emission - null
            Assert.assertNull(awaitItem())

            // Insert artwork
            repository.saveArtworks(listOf(artwork))
            val afterInsert = awaitItem()
            Assert.assertNotNull(afterInsert)
            Assert.assertEquals(artwork, afterInsert)

            // Update artwork
            val updated = artwork.copy(title = "Updated")
            repository.saveArtworks(listOf(updated))
            val afterUpdate = awaitItem()
            Assert.assertEquals("Updated", afterUpdate?.title)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowMovie_emits_updates_for_specific_movie() = runTest {
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie
        repository.saveArtworks(listOf(artwork))

        repository.flowMovie(artwork.id).test {
            // Initial emission - null
            Assert.assertNull(awaitItem())

            // Insert movie
            repository.saveMovies(listOf(movie))
            val afterInsert = awaitItem()
            Assert.assertNotNull(afterInsert)
            Assert.assertEquals(movie, afterInsert)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowEpisodes_emits_updates_for_specific_artwork() = runTest {
        val artwork = MediaMockups.showArtwork
        val episode1 = MediaMockups.episode1
        val episode2 = MediaMockups.episode2
        repository.saveArtworks(listOf(artwork))

        repository.flowEpisodes(artwork.id).test {
            // Initial emission - empty list
            Assert.assertEquals(emptyList<Episode>(), awaitItem())

            // Insert episodes
            repository.saveEpisodes(listOf(episode1))
            val afterFirst = awaitItem()
            Assert.assertEquals(1, afterFirst.size)

            repository.saveEpisodes(listOf(episode2))
            val afterSecond = awaitItem()
            Assert.assertEquals(2, afterSecond.size)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun flowSeasons_emits_updates_for_specific_artwork() = runTest {
        val artwork = MediaMockups.showArtwork
        val season1 = MediaMockups.season1
        val season2 = MediaMockups.season2
        repository.saveArtworks(listOf(artwork))

        repository.flowSeasons(artwork.id).test {
            // Initial emission - empty list
            Assert.assertEquals(emptyList<Season>(), awaitItem())

            // Insert season1
            repository.saveSeasons(listOf(season1))
            val afterFirst = awaitItem()
            Assert.assertEquals(1, afterFirst.size)
            Assert.assertEquals(season1, afterFirst.first())

            // Insert season2
            repository.saveSeasons(listOf(season2))
            val afterSecond = awaitItem()
            Assert.assertEquals(2, afterSecond.size)

            cancelAndConsumeRemainingEvents()
        }
    }

    // endregion

    // region Edge Cases

    @Test
    fun deleteArtworks_with_empty_list_does_not_affect_data() = runTest {
        // Given
        repository.saveArtworks(MediaMockups.artworks)

        // When
        repository.deleteArtworks(emptyList())

        // Then
        Assert.assertEquals(MediaMockups.artworks.size, repository.getArtworks().size)
    }

    @Test
    fun deleteMovies_with_empty_list_does_not_affect_data() = runTest {
        // Given
        repository.saveArtworks(listOf(MediaMockups.movieArtwork))
        repository.saveMovies(MediaMockups.movies)

        // When
        repository.deleteMovies(emptyList())

        // Then
        Assert.assertEquals(1, repository.getMovies().size)
    }

    @Test
    fun deleteEpisodes_with_empty_list_does_not_affect_data() = runTest {
        // Given
        repository.saveArtworks(listOf(MediaMockups.showArtwork))
        repository.saveEpisodes(MediaMockups.episodes)
        repository.saveSeasons(MediaMockups.seasons)

        // When
        repository.deleteEpisodes(emptyList())

        // Then
        Assert.assertEquals(3, repository.getEpisodes().size)
        Assert.assertEquals(2, repository.getSeasons().size)
    }

    @Test
    fun getSeasons_for_nonexistent_artworkId_returns_empty_list() = runTest {
        // When
        val result = repository.getSeasons(999999L)

        // Then
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun getEpisodes_for_nonexistent_artworkId_returns_empty_list() = runTest {
        // When
        val result = repository.getEpisodes(999999L)

        // Then
        Assert.assertTrue(result.isEmpty())
    }

    // endregion

}