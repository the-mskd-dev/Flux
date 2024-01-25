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

/*
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
interface DatabaseDao {

    @Insert
    suspend fun insertMovie(movie: FluxMovie)

    @Insert
    suspend fun insertShow(show: FluxShow)

    @Insert
    suspend fun insertEpisode(episode: FluxEpisode)

    @Query("SELECT * FROM movieentity")
    suspend fun getMovies() : List<FluxMovie>

    @Query("SELECT * FROM showentity")
    suspend fun getShows() : List<FluxShow>

    @Query("SELECT * FROM episodeentity")
    suspend fun getEpisodes() : List<FluxEpisode>

    @Query("SELECT * FROM episodeentity WHERE showId LIKE :showId")
    suspend fun getEpisodesFor(showId: Int) : List<FluxEpisode>

    @Delete
    suspend fun deleteMovie(movie: FluxMovie)

    @Delete
    suspend fun deleteShow(show: FluxShow)

    @Delete
    suspend fun deleteEpisode(episode: FluxEpisode)

}

@Database(entities = [
    MovieEntity::class,
    ShowEntity::class,
    EpisodeEntity::class
 ], version = 1)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun fluxDao(): DatabaseDao

}*/