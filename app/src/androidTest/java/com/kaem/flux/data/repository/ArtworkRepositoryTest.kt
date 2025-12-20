package com.kaem.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.data.ddb.FluxDatabase
import com.kaem.flux.mockups.MediaMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@MediumTest
class ArtworkRepositoryTest {

    private lateinit var database: FluxDatabase
    private lateinit var db: DatabaseDao
    private lateinit var repository: ArtworkRepository


    @Before
    fun setUpDatabase() = runTest {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FluxDatabase::class.java
        ).build()

        db = database.dao()

        repository = ArtworkRepository(db)

        // Insert artworks
        db.insertArtworks(listOf(MediaMockups.movieArtwork, MediaMockups.showArtwork))

    }

    @After
    fun tearDown() {
        database.close()
    }


    @Test
    fun get_media_movie() = runTest {

        // Given
        db.insertMovies(listOf(MediaMockups.movie))

        // When
        val content = repository.getMedia(MediaMockups.movieArtwork.id)

        // Then
        assert(content.mediaArtwork == MediaMockups.movieArtwork)
        assert(content.movie == MediaMockups.movie)
    }

    @Test
    fun get_media_show() = runTest {

        // Given
        db.insertEpisodes(listOf(MediaMockups.episode1, MediaMockups.episode2))

        // When
        val content = repository.getMedia(MediaMockups.showArtwork.id)

        // Then
        assert(content.mediaArtwork == MediaMockups.showArtwork)
        assert(content.episodes?.size == 2)
    }

    @Test
    fun save_movie() = runTest {

        // Given
        val movie = MediaMockups.movie

        // When
        repository.saveMovie(movie)

        // Then
        val savedMovie = db.getMovie(movie.artworkId)
        assert(movie == savedMovie)
    }

    @Test
    fun save_episode() = runTest {

        // Given
        val episode = MediaMockups.episode1

        // When
        repository.saveEpisode(episode)

        // Then
        val savedEpisode = db.getEpisode(episode.id)
        assert(episode == savedEpisode)
    }

    @Test
    fun save_episodes() = runTest {

        // Given
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)

        // When
        repository.saveEpisodes(episodes)

        // Then
        val savedEpisodes = db.getEpisodes(episodes.first().artworkId)
        assert(episodes == savedEpisodes)
    }
}