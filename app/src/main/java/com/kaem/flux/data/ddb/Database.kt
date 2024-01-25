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
    suspend fun insertMovie(id: Int, content: String)

    @Insert
    suspend fun insertShow(id: Int, content: String)

    @Insert
    suspend fun insertEpisode(id: Int, showId: Int, content: String)

    @Query("SELECT * FROM movies")
    suspend fun getMovies() : List<MovieEntity>

    @Query("SELECT * FROM shows")
    suspend fun getShows() : List<ShowEntity>

    @Query("SELECT * FROM episodes")
    suspend fun getEpisodes() : List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE showId LIKE :showId")
    suspend fun getEpisodesFor(showId: Int) : List<EpisodeEntity>

    @Query("DELETE FROM movies WHERE id LIKE :id")
    suspend fun deleteMovie(id: Int)

    @Query("DELETE FROM episodes WHERE id LIKE :id")
    suspend fun deleteShow(id: Int)

    @Query("DELETE FROM episodes WHERE showId LIKE :showId")
    suspend fun deleteEpisodesForShow(showId: Int)

    @Query("DELETE FROM episodes WHERE id LIKE :id")
    suspend fun deleteEpisode(id: Int)

}

@Database(entities = [
    MovieEntity::class,
    ShowEntity::class,
    EpisodeEntity::class
 ], version = 1)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun fluxDao(): DatabaseDao

}