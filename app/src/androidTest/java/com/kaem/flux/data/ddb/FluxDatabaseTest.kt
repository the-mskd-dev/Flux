package com.kaem.flux.data.ddb

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.kaem.flux.mockups.ArtworkMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class FluxDatabaseTest {

    private lateinit var database: FluxDatabase
    private lateinit var db: FluxDao

    @Before
    fun setUpDatabase() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FluxDatabase::class.java
        ).build()

        db = database.fluxDao()

    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insert_movie() = runTest {

        // Given
        val overview = ArtworkMockups.movieOverview
        val movie = ArtworkMockups.movie

        // When
        db.insertOverviews(listOf(overview))
        db.insertMovies(listOf(movie))

        // Then
        val dbOverview = db.getOverview(overview.id)
        val dbMovie = db.getMovie(overview.id)
        assert(dbOverview == overview)
        assert(dbMovie == movie)

    }

    @Test
    fun delete_movie_by_overview() = runTest {

        // Given
        val overview = ArtworkMockups.movieOverview
        val movie = ArtworkMockups.movie

        // When
        db.insertOverviews(listOf(overview))
        db.insertMovies(listOf(movie))
        db.deleteOverviews(listOf(overview.id))

        // Then
        val dbOverview = db.getOverview(overview.id)
        val dbMovie = db.getMovie(overview.id)
        assert(dbOverview == null)
        assert(dbMovie == null)

    }

    @Test
    fun delete_movie_by_artwork() = runTest {

        // Given
        val overview = ArtworkMockups.movieOverview
        val movie = ArtworkMockups.movie

        // When
        db.insertOverviews(listOf(overview))
        db.insertMovies(listOf(movie))
        db.deleteMovies(listOf(movie))

        // Then
        val dbOverview = db.getOverview(overview.id)
        val dbMovie = db.getMovie(overview.id)
        assert(dbOverview == null)
        assert(dbMovie == null)

    }

    @Test
    fun insert_show() = runTest {

        // Given
        val overview = ArtworkMockups.showOverview
        val episode1 = ArtworkMockups.episode1
        val episode2 = ArtworkMockups.episode2

        // When
        db.insertOverviews(listOf(overview))
        db.insertEpisodes(listOf(episode1, episode2))

        // Then
        val dbOverview = db.getOverview(overview.id)
        val dbEpisodes = db.getEpisodes(overview.id)
        assert(dbOverview == overview)
        assert(dbEpisodes.size == 2)
        assert(dbEpisodes == listOf(episode1, episode2))

    }

    @Test
    fun delete_show_by_overview() = runTest {

        // Given
        val overview = ArtworkMockups.showOverview
        val episode1 = ArtworkMockups.episode1
        val episode2 = ArtworkMockups.episode2

        // When
        db.insertOverviews(listOf(overview))
        db.insertEpisodes(listOf(episode1, episode2))
        db.deleteOverviews(listOf(overview.id))

        // Then
        val dbOverview = db.getOverview(overview.id)
        val dbEpisodes = db.getEpisodes(overview.id)
        assert(dbOverview == null)
        assert(dbEpisodes.isEmpty())

    }

    @Test
    fun delete_show_by_episodes() = runTest {

        // Given
        val overview = ArtworkMockups.showOverview
        val episode1 = ArtworkMockups.episode1
        val episode2 = ArtworkMockups.episode2

        // When
        db.insertOverviews(listOf(overview))
        db.insertEpisodes(listOf(episode1, episode2))
        db.deleteEpisodes(listOf(episode1))

        // Then
        val dbOverview = db.getOverview(overview.id)
        val dbEpisodes = db.getEpisodes(overview.id)
        assert(dbOverview == overview)
        assert(dbEpisodes == listOf(episode2))

        db.deleteEpisodes(listOf(episode2))
        val dbOverview2 = db.getOverview(overview.id)
        val dbEpisodes2 = db.getEpisodes(overview.id)
        assert(dbOverview2 == null)
        assert(dbEpisodes2.isEmpty())

    }

    @Test
    fun delete_show_by_all_episodes() = runTest {

        // Given
        val overview = ArtworkMockups.showOverview
        val episode1 = ArtworkMockups.episode1
        val episode2 = ArtworkMockups.episode2

        // When
        db.insertOverviews(listOf(overview))
        db.insertEpisodes(listOf(episode1, episode2))
        db.deleteEpisodes(listOf(episode1, episode2))

        // Then
        val dbOverview = db.getOverview(overview.id)
        val dbEpisodes = db.getEpisodes(overview.id)
        assert(dbOverview == null)
        assert(dbEpisodes.isEmpty())

    }

    @Test
    fun get_all_file_names() = runTest {

        // Given
        val overviews = listOf(ArtworkMockups.movieOverview, ArtworkMockups.showOverview)
        val movie = ArtworkMockups.movie
        val episodes = listOf(ArtworkMockups.episode1, ArtworkMockups.episode2)
        val fileNames = episodes.map { it.file.name } + movie.file.name

        // When
        db.insertOverviews(overviews)
        db.insertMovies(listOf(movie))
        db.insertEpisodes(episodes)

        // Then
        val dbFileNames = db.getAllFileNames()
        assert(dbFileNames.containsAll(fileNames))

    }

    @Test
    fun delete_artworks_with_no_files() = runTest {

        // Given
        val overviews = listOf(ArtworkMockups.movieOverview, ArtworkMockups.showOverview)
        val movie = ArtworkMockups.movie
        val episodes = listOf(ArtworkMockups.episode1, ArtworkMockups.episode2)
        val files = listOf(ArtworkMockups.episode2.file)

        // When
        db.insertOverviews(overviews)
        db.insertMovies(listOf(movie))
        db.insertEpisodes(episodes)
        db.deleteArtworksWithNoFiles(files)

        // Then
        val dbOverviews = db.getOverviews()
        val dbMovies = db.getMovies()
        val dbEpisodes = db.getEpisodes()
        assert(dbOverviews.size == 1)
        assert(dbOverviews.contains(ArtworkMockups.showOverview))
        assert(dbMovies.isEmpty())
        assert(dbEpisodes.size == 1)
        assert(dbEpisodes.contains(ArtworkMockups.episode2))

    }

}