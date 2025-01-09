package com.kaem.flux.ddb

import androidx.room.Embedded
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.ddb.FluxDatabase
import com.kaem.flux.mockups.ArtworkMockups
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
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
    fun insert_and_delete_movie() = runBlocking {

        val overview = ArtworkMockups.movieOverview
        val movie = ArtworkMockups.movie


        db.insertOverviews(listOf(overview))
        val dbOverview = db.getOverview(overview.id)

        assert(overview == dbOverview)

        db.insertMovies(listOf(movie))
        val dbMovie = db.getMovie(overview.id)

        assert(movie == dbMovie)

        db.deleteOverviews(listOf(overview.id))

        val deletedOverview = db.getOverview(overview.id)
        val deletedMovie = db.getMovie(overview.id)

        assert(deletedOverview == null)
        assert(deletedMovie == null)

    }

    @Test
    fun insert_and_delete_show() = runBlocking {

        val overview = ArtworkMockups.showOverview
        val episode1 = ArtworkMockups.episode1
        val episode2 = ArtworkMockups.episode2

        db.insertOverviews(listOf(overview))
        val dbOverview = db.getOverview(overview.id)

        assert(overview == dbOverview)


        db.insertEpisodes(listOf(episode1, episode2))
        val dbEpisodes = db.getEpisodes(overview.id)

        assert(dbEpisodes.size == 2)
        assert(dbEpisodes.any { it == episode1 })
        assert(dbEpisodes.any { it == episode2 })

        db.deleteEpisode(episode2.id)
        var dbEpisodesCount = db.getEpisodeCountByOverviewId(overview.id)

        assert(dbEpisodesCount == 1)

        db.deleteOverviews(listOf(overview.id))

        val deletedOverview = db.getOverview(overview.id)
        dbEpisodesCount = db.getEpisodeCountByOverviewId(overview.id)

        assert(deletedOverview == null)
        assert(dbEpisodesCount == 0)

    }

    @Test
    fun insert_and_delete_episodes() = runBlocking {

        val overview = ArtworkMockups.showOverview
        val episode1 = ArtworkMockups.episode1
        val episode2 = ArtworkMockups.episode2

        db.insertOverviews(listOf(overview))
        db.insertEpisodes(listOf(episode1, episode2))
        var dbOverview = db.getOverview(overview.id)

        assert(overview == dbOverview)

        db.deleteEpisode(episode1)
        dbOverview = db.getOverview(overview.id)
        var dbEpisodesCount = db.getEpisodeCountByOverviewId(overview.id)

        assert(overview == dbOverview)
        assert(dbEpisodesCount == 1)

        db.deleteEpisode(episode2)
        dbOverview = db.getOverview(overview.id)
        dbEpisodesCount = db.getEpisodeCountByOverviewId(overview.id)

        assert(dbOverview == null)
        assert(dbEpisodesCount == 0)

    }

    @After
    fun closeDatabase() {
        database.close()
    }

}