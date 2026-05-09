package com.mskd.flux.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import app.cash.turbine.test
import com.mskd.flux.data.ddb.FluxDatabase
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.artwork.ArtworkRepositoryImpl
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import com.mskd.flux.mockups.MediaMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@MediumTest
class ArtworkRepositoryTest {

    private lateinit var database: FluxDatabase
    private lateinit var artworkRepository: ArtworkRepository
    private lateinit var databaseRepository: DatabaseRepository


    @Before
    fun setUpDatabase() = runTest {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FluxDatabase::class.java
        ).build()

        databaseRepository = DatabaseRepositoryImpl(dao = database.dao())

        artworkRepository = ArtworkRepositoryImpl(database = databaseRepository)

        // Insert artworks
        databaseRepository.saveArtworks(listOf(MediaMockups.movieArtwork, MediaMockups.showArtwork))

    }

    @After
    fun tearDown() {
        database.close()
    }


    @Test
    fun get_media_movie() = runTest {

        // Given
        databaseRepository.saveMovies(listOf(MediaMockups.movie))

        artworkRepository.flow.test {

            // When
            artworkRepository.searchArtwork(MediaMockups.movieArtwork.id)
            val content = awaitItem() as? ArtworkRepository.Content.MOVIE

            // Then
            assert(content != null)
            assert(content?.artwork == MediaMockups.movieArtwork)
            assert(content?.movie == MediaMockups.movie)

        }
    }

    @Test
    fun get_media_show() = runTest {

        // Given
        databaseRepository.saveEpisodes(listOf(MediaMockups.episode1, MediaMockups.episode2))

        artworkRepository.flow.test {

            // When
            artworkRepository.searchArtwork(MediaMockups.showArtwork.id)
            val content = awaitItem() as? ArtworkRepository.Content.SHOW

            // Then
            assert(content != null)
            assert(content?.artwork == MediaMockups.showArtwork)
            assert(content?.episodes?.size == 2)

        }

    }

    @Test
    fun save_movie() = runTest {

        // Given
        val movie = MediaMockups.movie

        // When
        artworkRepository.saveMovie(movie)

        // Then
        val savedMovie = databaseRepository.getMovie(movie.artworkId)
        assert(movie == savedMovie)
    }

    @Test
    fun save_episode() = runTest {

        // Given
        val episode = MediaMockups.episode1

        // When
        artworkRepository.saveEpisode(episode)

        // Then
        val savedEpisode = databaseRepository.getEpisode(episode.id)
        assert(episode == savedEpisode)
    }

    @Test
    fun save_episodes() = runTest {

        // Given
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)

        // When
        artworkRepository.saveEpisodes(episodes)

        // Then
        val savedEpisodes = databaseRepository.getEpisodes(episodes.first().artworkId)
        assert(episodes == savedEpisodes)
    }
}