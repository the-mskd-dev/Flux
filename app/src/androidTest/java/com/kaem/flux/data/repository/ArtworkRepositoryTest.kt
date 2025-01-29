package com.kaem.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.ddb.FluxDatabase
import com.kaem.flux.mockups.ArtworkMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@MediumTest
class ArtworkRepositoryTest {

    private lateinit var database: FluxDatabase
    private lateinit var db: FluxDao
    private lateinit var repository: ArtworkRepository


    @Before
    fun setUpDatabase() = runTest {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FluxDatabase::class.java
        ).build()

        db = database.fluxDao()

        repository = ArtworkRepository(db)

        // Insert overviews
        db.insertOverviews(listOf(ArtworkMockups.movieOverview, ArtworkMockups.showOverview))

    }

    @After
    fun tearDown() {
        database.close()
    }


    @Test
    fun get_artwork_movie() = runTest {

        // Given
        db.insertMovies(listOf(ArtworkMockups.movie))

        // When
        val content = repository.getArtwork(ArtworkMockups.movieOverview.id)

        // Then
        assert(content.artworkOverview == ArtworkMockups.movieOverview)
        assert(content.movie == ArtworkMockups.movie)
    }

    @Test
    fun get_artwork_show() = runTest {

        // Given
        db.insertEpisodes(listOf(ArtworkMockups.episode1, ArtworkMockups.episode2))

        // When
        val content = repository.getArtwork(ArtworkMockups.showOverview.id)

        // Then
        assert(content.artworkOverview == ArtworkMockups.showOverview)
        assert(content.episodes?.size == 2)
    }

    @Test
    fun save_movie() = runTest {

        // Given
        val movie = ArtworkMockups.movie

        // When
        repository.saveMovie(movie)

        // Then
        val savedMovie = db.getMovie(movie.artworkId)
        assert(movie == savedMovie)
    }

    @Test
    fun save_episode() = runTest {

        // Given
        val episode = ArtworkMockups.episode1

        // When
        repository.saveEpisode(episode)

        // Then
        val savedEpisode = db.getEpisode(episode.id)
        assert(episode == savedEpisode)
    }

    @Test
    fun save_episodes() = runTest {

        // Given
        val episodes = listOf(ArtworkMockups.episode1, ArtworkMockups.episode2)

        // When
        repository.saveEpisodes(episodes)

        // Then
        val savedEpisodes = db.getEpisodes(episodes.first().artworkId)
        assert(episodes == savedEpisodes)
    }
}