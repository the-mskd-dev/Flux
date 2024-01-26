package com.kaem.flux.data.ddb

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow

@Dao
interface FluxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: FluxMovie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShow(show: FluxShow)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Query("DELETE FROM fluxepisode WHERE showId LIKE :showId")
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
    abstract fun fluxDao(): FluxDao

}