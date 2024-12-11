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
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.ArtworkType
import com.kaem.flux.model.flux.ContentType
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Dao
interface FluxDao {

//region Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artworks: List<Artwork>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<UserFile>)

//endregion

//region Get

    @Query("SELECT * FROM artworks")
    suspend fun getArtworks() : List<Artwork>

    @Query("SELECT * FROM movies WHERE artworkId = :artworkId")
    suspend fun getMovie(artworkId: Long) : Movie

    @Query("SELECT * FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodes(artworkId: Long) : List<Episode>

    @Query("SELECT * FROM files WHERE id=:fileId")
    suspend fun getFile(fileId: Long) : UserFile

//endregion

//region Delete

    @Query("DELETE FROM artworks WHERE id IN (:ids)")
    suspend fun deleteArtworks(ids: List<Long>)

    @Query("DELETE FROM movies WHERE artworkId IN (:ids)")
    suspend fun deleteMovies(ids: List<Long>)

    @Query("DELETE FROM episodes WHERE artworkId IN (:ids)")
    suspend fun deleteEpisodes(ids: List<Long>)

    @Query("DELETE FROM files WHERE id IN (:ids)")
    suspend fun deleteFiles(ids: List<Long>)

    @Query("DELETE FROM episodes WHERE id = :episodeId")
    suspend fun deleteEpisode(episodeId: Long)

    @Transaction
    suspend fun deleteEpisode(episode: Episode) {
        // Delete episode
        deleteEpisode(episode.id)

        // Check if it remains episode for show
        val remainingEpisodes = getEpisodeCountByArtworkId(episode.artworkId)

        // If no, delete the show
        if (remainingEpisodes == 0) {
            deleteArtworks(listOf(episode.artworkId))
        }
    }

//endregion

//region Other

    @Query("SELECT COUNT(*) FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodeCountByArtworkId(artworkId: Long): Int

//endregion

}

class Converters {
    @TypeConverter
    fun fromContentType(contentType: ContentType): String {
        return contentType.name
    }

    @TypeConverter
    fun toContentType(value: String): ContentType {
        return ContentType.valueOf(value)
    }
}

@Database(entities = [
    Artwork::class,
    Movie::class,
    Episode::class,
    UserFile::class
 ], version = 1)
@TypeConverters(Converters::class)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun fluxDao(): FluxDao

}

class DatabaseManager(
    val fluxDao: FluxDao
) {

//region Save
    suspend fun saveArtworks(artworks: List<Artwork>) {
        fluxDao.insertArtworks(artworks)
    }

    suspend fun saveMovies(movies: List<Movie>) {
        fluxDao.insertMovies(movies)
    }

    suspend fun saveEpisodes(episodes: List<Episode>) {
        fluxDao.insertEpisodes(episodes)
    }

    suspend fun saveFiles(files: List<UserFile>) {
        fluxDao.insertFiles(files)
    }

//endregion

//region Get

    suspend fun getArtworks() : List<Artwork> {
        return fluxDao.getArtworks()
    }

    suspend fun getMovieFrom(artworkId: Long) : Movie {
        return fluxDao.getMovie(artworkId)
    }

    suspend fun getEpisodesFrom(artworkId: Long) : List<Episode> {
        return fluxDao.getEpisodes(artworkId)
    }

    suspend fun getFileFrom(fileId: Long) : UserFile {
        return fluxDao.getFile(fileId)
    }

//endregion

//region Delete

    suspend fun deleteArtworks(ids: List<Long>) {
        fluxDao.deleteArtworks(ids)
    }

    suspend fun deleteMovies(ids: List<Long>) {
        fluxDao.deleteMovies(ids)
    }


    suspend fun deleteEpisodes(ids: List<Long>) {
        fluxDao.deleteEpisodes(ids)
    }

    suspend fun deleteFiles(ids: List<Long>) {
        fluxDao.deleteFiles(ids)
    }

//endregion

}