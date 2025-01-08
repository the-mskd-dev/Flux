package com.kaem.flux.ddb

import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.ddb.FluxDatabase
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class DatabaseTest {

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

    @Test
    fun test_1_insert_movie_overview() = runBlocking {

        val overview = ArtworkOverview(
            id = 1L,
            title = "Your name",
            type = ContentType.MOVIE
        )

        db.insertOverviews(listOf(overview))

        val dbOverview = db.getOverview(overview.id)

        assert(overview == dbOverview)

    }

    @Test
    fun test_2_insert_Movie() = runBlocking {

        val movie = Movie(
            artworkId = 1L,
            title = "Your name",
            releaseDateString = "2016-12-28",
            description = "description",
            voteAverage = 10f,
            voteCount = 2809,
            duration = 110,
            currentTime = 0L,
            status = Status.TO_WATCH,
            file = UserFile(
                name = "your_name.mkv",
                addedDateTime = 0L,
                path = "path",
                source = FileSource.LOCAL
            )
        )

        db.insertMovies(listOf(movie))
        val dbMovie = db.getMovie(artworkId = movie.artworkId)

        assert(movie == dbMovie)

    }

    @Test
    fun test_3_insert_show_overview() = runBlocking {

        val overview = ArtworkOverview(
            id = 2L,
            title = "Naruto",
            type = ContentType.SHOW
        )

        db.insertOverviews(listOf(overview))
        val dbOverview = db.getOverview(overview.id)

        assert(overview == dbOverview)

    }

    @Test
    fun test_4_insert_show_episodes() = runBlocking {

        val episode1 = Episode(
            id = 3L,
            number = 1,
            season = 1,
            imagePath = "",
            artworkId = 2L,
            title = "Naruto episode 1",
            releaseDateString = "2016-12-28",
            description = "description",
            voteAverage = 10f,
            voteCount = 2809,
            duration = 22,
            currentTime = 0L,
            status = Status.TO_WATCH,
            file = UserFile(
                name = "naruto_S01E01.mkv",
                addedDateTime = 0L,
                path = "path",
                source = FileSource.LOCAL
            )
        )

        val episode2 = Episode(
            id = 4L,
            number = 2,
            season = 1,
            imagePath = "",
            artworkId = 2L,
            title = "Naruto epsiode 2",
            releaseDateString = "2016-12-28",
            description = "description",
            voteAverage = 10f,
            voteCount = 289,
            duration = 23,
            currentTime = 0L,
            status = Status.TO_WATCH,
            file = UserFile(
                name = "naruto_S01E02.mkv",
                addedDateTime = 0L,
                path = "path",
                source = FileSource.LOCAL
            )
        )

        db.insertEpisodes(listOf(episode1, episode2))
        val dbEpisodes = db.getEpisodes(artworkId = episode1.artworkId)

        assert(dbEpisodes.size == 2)
        assert(dbEpisodes.any { it == episode1 })
        assert(dbEpisodes.any { it == episode2 })

    }

    @Test
    fun test_5_delete_movie_overview() = runBlocking {

        db.deleteOverviews(listOf(1L))

        val dbOverview = db.getOverview(1L)
        val dbMovie = db.getMovie(1L)

        assert(dbOverview == null)
        assert(dbMovie == null)

    }

    @Test
    fun test_5_delete_show_overview() = runBlocking {

        db.deleteOverviews(listOf(2L))

        val dbOverview = db.getOverview(2L)
        val dbEpisodes = db.getEpisodes(2L)

        assert(dbOverview == null)
        assert(dbEpisodes.isEmpty())

    }

    @After
    fun closeDatabase() {
        database.close()
    }

}