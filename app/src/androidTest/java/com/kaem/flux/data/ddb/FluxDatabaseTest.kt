package com.kaem.flux.data.ddb

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.kaem.flux.mockups.MediaMockups
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class FluxDatabaseTest {

    private lateinit var database: FluxDatabase
    private lateinit var db: DatabaseDao

    @Before
    fun setUpDatabase() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FluxDatabase::class.java
        ).build()

        db = database.dao()

    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insert_movie() = runTest {

        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie

        // When
        db.insertArtworks(listOf(artwork))
        db.insertMovies(listOf(movie))

        // Then
        val dbArtwork = db.getArtwork(artwork.id)
        val dbMovie = db.getMovie(artwork.id)
        assert(dbArtwork == artwork)
        assert(dbMovie == movie)

    }

    @Test
    fun delete_movie_by_artwork() = runTest {

        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie

        // When
        db.insertArtworks(listOf(artwork))
        db.insertMovies(listOf(movie))
        db.deleteArtworks(listOf(artwork.id))

        // Then
        val dbArtwork = db.getArtwork(artwork.id)
        val dbMovie = db.getMovie(artwork.id)
        assert(dbArtwork == null)
        assert(dbMovie == null)

    }

    @Test
    fun delete_movie_by_media() = runTest {

        // Given
        val artwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie

        // When
        db.insertArtworks(listOf(artwork))
        db.insertMovies(listOf(movie))
        db.deleteMovies(listOf(movie))

        // Then
        val dbArtwork = db.getArtwork(artwork.id)
        val dbMovie = db.getMovie(artwork.id)
        assert(dbArtwork == null)
        assert(dbMovie == null)

    }

    @Test
    fun insert_show() = runTest {

        // Given
        val artwork = MediaMockups.showArtwork
        val episode1 = MediaMockups.episode1
        val episode2 = MediaMockups.episode2

        // When
        db.insertArtworks(listOf(artwork))
        db.insertEpisodes(listOf(episode1, episode2))

        // Then
        val dbArtwork = db.getArtwork(artwork.id)
        val dbEpisodes = db.getEpisodes(artwork.id)
        assert(dbArtwork == artwork)
        assert(dbEpisodes.size == 2)
        assert(dbEpisodes == listOf(episode1, episode2))

    }

    @Test
    fun delete_show_by_artwork() = runTest {

        // Given
        val artwork = MediaMockups.showArtwork
        val episode1 = MediaMockups.episode1
        val episode2 = MediaMockups.episode2

        // When
        db.insertArtworks(listOf(artwork))
        db.insertEpisodes(listOf(episode1, episode2))
        db.deleteArtworks(listOf(artwork.id))

        // Then
        val dbArtwork = db.getArtwork(artwork.id)
        val dbEpisodes = db.getEpisodes(artwork.id)
        assert(dbArtwork == null)
        assert(dbEpisodes.isEmpty())

    }

    @Test
    fun delete_show_by_episodes() = runTest {

        // Given
        val artwork = MediaMockups.showArtwork
        val episode1 = MediaMockups.episode1
        val episode2 = MediaMockups.episode2

        // When
        db.insertArtworks(listOf(artwork))
        db.insertEpisodes(listOf(episode1, episode2))
        db.deleteEpisodes(listOf(episode1))

        // Then
        val dbArtwork = db.getArtwork(artwork.id)
        val dbEpisodes = db.getEpisodes(artwork.id)
        assert(dbArtwork == artwork)
        assert(dbEpisodes == listOf(episode2))

        db.deleteEpisodes(listOf(episode2))
        val dbArtwork2 = db.getArtwork(artwork.id)
        val dbEpisodes2 = db.getEpisodes(artwork.id)
        assert(dbArtwork2 == null)
        assert(dbEpisodes2.isEmpty())

    }

    @Test
    fun delete_show_by_all_episodes() = runTest {

        // Given
        val artwork = MediaMockups.showArtwork
        val episode1 = MediaMockups.episode1
        val episode2 = MediaMockups.episode2

        // When
        db.insertArtworks(listOf(artwork))
        db.insertEpisodes(listOf(episode1, episode2))
        db.deleteEpisodes(listOf(episode1, episode2))

        // Then
        val dbArtwork = db.getArtwork(artwork.id)
        val dbEpisodes = db.getEpisodes(artwork.id)
        assert(dbArtwork == null)
        assert(dbEpisodes.isEmpty())

    }

    @Test
    fun get_all_file_names() = runTest {

        // Given
        val artworks = listOf(MediaMockups.movieArtwork, MediaMockups.showArtwork)
        val movie = MediaMockups.movie
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        val fileNames = episodes.map { it.file.name } + movie.file.name

        // When
        db.insertArtworks(artworks)
        db.insertMovies(listOf(movie))
        db.insertEpisodes(episodes)

        // Then
        val dbFileNames = db.getAllFileNames()
        assert(dbFileNames.containsAll(fileNames))

    }

    @Test
    fun delete_media_with_no_files() = runTest {

        // Given
        val artworks = listOf(MediaMockups.movieArtwork, MediaMockups.showArtwork)
        val movie = MediaMockups.movie
        val episodes = listOf(MediaMockups.episode1, MediaMockups.episode2)
        val files = listOf(MediaMockups.episode2.file)

        // When
        db.insertArtworks(artworks)
        db.insertMovies(listOf(movie))
        db.insertEpisodes(episodes)
        db.deleteMediasWithNoFiles(files)

        // Then
        val dbArtworks = db.getArtworks()
        val dbMovies = db.getMovies()
        val dbEpisodes = db.getEpisodes()
        assert(dbArtworks.size == 1)
        assert(dbArtworks.contains(MediaMockups.showArtwork))
        assert(dbMovies.isEmpty())
        assert(dbEpisodes.size == 1)
        assert(dbEpisodes.contains(MediaMockups.episode2))

    }

}