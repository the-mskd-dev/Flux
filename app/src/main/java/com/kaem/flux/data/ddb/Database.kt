package com.kaem.flux.data.ddb

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.google.gson.Gson
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Entity
data class MovieEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val content: String
)

@Entity
data class ShowEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val content: String
)

@Entity
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val showId: Int,
    @ColumnInfo val content: String
)

@Dao
interface FluxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShows(shows: List<ShowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("SELECT * FROM movieentity")
    suspend fun getMovies() : List<MovieEntity>

    @Query("SELECT * FROM showentity")
    suspend fun getShows() : List<ShowEntity>

    @Query("SELECT * FROM EpisodeEntity")
    suspend fun getEpisodes() : List<EpisodeEntity>

    @Query("DELETE FROM movieentity WHERE id IN (:ids)")
    suspend fun deleteMovies(ids: List<Int>)

    @Query("DELETE FROM showentity WHERE id IN (:ids)")
    suspend fun deleteShows(ids: List<Int>)

    @Query("DELETE FROM episodeentity WHERE id IN (:ids)")
    suspend fun deleteEpisodes(ids: List<Int>)

}

@Database(entities = [
    MovieEntity::class,
    ShowEntity::class,
    EpisodeEntity::class
 ], version = 1)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun fluxDao(): FluxDao

}

class DatabaseManager(
    val gson: Gson,
    val fluxDao: FluxDao
) {

    suspend fun saveArtworks(artworks: List<FluxArtwork>) {

        val movies = artworks.filterIsInstance<FluxMovie>()
        val shows = artworks.filterIsInstance<FluxShow>()

        withContext(Dispatchers.Default) {

            launch {

                val movieEntities = movies.map {

                    val content = gson.toJson(it)

                    MovieEntity(
                        id = it.id,
                        content = content
                    )

                }

                withContext(Dispatchers.IO) { fluxDao.insertMovies(movieEntities) }

            }



            launch {

                val showEntities = shows.map {

                    val content = gson.toJson(it)

                    ShowEntity(
                        id = it.id,
                        content = content
                    )

                }

                withContext(Dispatchers.IO) { fluxDao.insertShows(showEntities) }

            }

        }

    }

    suspend fun saveEpisodes(episodes: List<FluxEpisode>) {

        val episodeEntities = episodes.map {

            val content = withContext(Dispatchers.Default) { gson.toJson(it) }

            EpisodeEntity(
                id = it.id,
                showId = it.showId,
                content = content
            )

        }

        withContext(Dispatchers.IO) { fluxDao.insertEpisodes(episodeEntities) }

    }

    suspend fun getAllArtworks() : List<FluxArtwork> {

        var movies = listOf<FluxMovie>()
        var shows = listOf<FluxShow>()

        coroutineScope {

            launch {

                val movieEntities = withContext(Dispatchers.IO) { fluxDao.getMovies() }

                movies = movieEntities.map {

                    async {

                        withContext(Dispatchers.Default) { gson.fromJson(it.content, FluxMovie::class.java) }

                    }

                }.awaitAll()

            }


            launch {

                val showEntities = withContext(Dispatchers.IO) { fluxDao.getShows() }

                shows = showEntities.map {

                    async {

                        withContext(Dispatchers.Default) { gson.fromJson(it.content, FluxShow::class.java) }

                    }

                }.awaitAll()

            }

        }

        return movies + shows

    }

    suspend fun getAllEpisodes() : List<FluxEpisode> {

        var episodes = listOf<FluxEpisode>()

        coroutineScope {

            launch {

                val episodeEntities = withContext(Dispatchers.IO) { fluxDao.getEpisodes() }

                episodes = episodeEntities.map {

                    async {

                        withContext(Dispatchers.Default) { gson.fromJson(it.content, FluxEpisode::class.java) }

                    }

                }.awaitAll()

            }

        }

        return episodes

    }

    suspend fun deleteMovies(ids: List<Int>) {
        withContext(Dispatchers.IO) { fluxDao.deleteMovies(ids) }
    }

    suspend fun deleteShows(ids: List<Int>) {
        withContext(Dispatchers.IO) { fluxDao.deleteShows(ids) }
    }

    suspend fun deleteEpisodes(ids: List<Int>) {
        withContext(Dispatchers.IO) { fluxDao.deleteEpisodes(ids) }
    }

}