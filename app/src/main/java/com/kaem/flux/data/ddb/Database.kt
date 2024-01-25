package com.kaem.flux.data.ddb

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow


@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val content: String
)

@Entity(tableName = "shows")
data class ShowEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val content: String
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val showId: Int,
    @ColumnInfo val content: String
)

@Dao
interface DatabaseDao {

    @Insert
    suspend fun insertMovie(movie: FluxMovie)

    @Insert
    suspend fun insertShow(show: FluxShow)

    @Insert
    suspend fun insertEpisode(episode: FluxEpisode)

    @Query("SELECT * FROM fluxmovie")
    suspend fun getMovies() : List<FluxMovie>

    @Query("SELECT * FROM fluxshow")
    suspend fun getShows() : List<FluxShow>

    @Query("SELECT * FROM fluxepisode")
    suspend fun getEpisodes() : List<FluxEpisode>

    @Query("SELECT * FROM fluxepisode WHERE showId LIKE :showId")
    suspend fun getEpisodesFor(showId: Int) : List<FluxEpisode>

    @Delete
    suspend fun deleteMovie(fluxMovie: FluxMovie)

    @Delete
    suspend fun deleteShow(fluxShow: FluxShow)

    @Query("DELETE FROM episodes WHERE showId LIKE :showId")
    suspend fun deleteEpisodesForShow(showId: Int)

    @Delete
    suspend fun deleteEpisode(fluxEpisode: FluxEpisode)

}

@Database(entities = [
    FluxMovie::class,
    FluxShow::class,
    FluxEpisode::class
 ], version = 1)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun fluxDao(): DatabaseDao

}