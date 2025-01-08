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
    fun insertOverview() = runBlocking {

        val overview = ArtworkOverview(
            id = 0L,
            title = "Your name",
            type = ContentType.SHOW
        )

        db.insertOverviews(listOf(overview))

        val overviews = db.getOverviews()

        assert(overviews.size == 1)
        assert(overviews.first() == overview)

    }

    @Test
    fun insertMovie() = runBlocking {

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

        val movies = db.getMovies()

        assert(movies.size == 1)
        assert(movies.first() == movie)

    }

    @After
    fun closeDatabase() {
        database.close()
    }

}