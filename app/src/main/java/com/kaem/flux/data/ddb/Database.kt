package com.kaem.flux.data.ddb

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.flux.ArtworkOverview
import com.kaem.flux.model.flux.ContentType
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie

@Dao
interface FluxDao {

//region Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artworkOverviews: List<ArtworkOverview>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

//endregion

//region Get

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    suspend fun getArtwork(artworkId: Long) : ArtworkOverview

    @Query("SELECT * FROM artworks")
    suspend fun getArtworks() : List<ArtworkOverview>

    @Query("SELECT * FROM movies")
    suspend fun getMovies() : List<Movie>

    @Query("SELECT * FROM movies WHERE artworkId = :artworkId")
    suspend fun getMovie(artworkId: Long) : Movie

    @Query("SELECT * FROM episodes")
    suspend fun getEpisodes() : List<Episode>

    @Query("SELECT * FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodes(artworkId: Long) : List<Episode>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisode(episodeId: Long) : Episode

//endregion

//region Delete

    @Query("DELETE FROM artworks WHERE id IN (:ids)")
    suspend fun deleteArtworks(ids: List<Long>)

    @Query("DELETE FROM movies WHERE artworkId IN (:ids)")
    suspend fun deleteMovies(ids: List<Long>)

    @Query("DELETE FROM episodes WHERE artworkId IN (:ids)")
    suspend fun deleteEpisodes(ids: List<Long>)

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

    @TypeConverter
    fun fromFileSource(fileSource: FileSource): String {
        return fileSource.name
    }

    @TypeConverter
    fun toFileSource(value: String): FileSource {
        return FileSource.valueOf(value)
    }
}

@Database(entities = [
    ArtworkOverview::class,
    Movie::class,
    Episode::class
 ], version = 1)
@TypeConverters(Converters::class)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun fluxDao(): FluxDao

}

class DatabaseManager(
    val fluxDao: FluxDao
) {

//region Save
    suspend fun saveArtworks(artworkOverviews: List<ArtworkOverview>) {
        fluxDao.insertArtworks(artworkOverviews)
    }

    suspend fun saveMovies(movies: List<Movie>) {
        fluxDao.insertMovies(movies)
    }

    suspend fun saveEpisodes(episodes: List<Episode>) {
        fluxDao.insertEpisodes(episodes)
    }

//endregion

//region Get

    suspend fun getArtwork(artworkId: Long) : ArtworkOverview {
        return fluxDao.getArtwork(artworkId)
    }

    suspend fun getArtworks() : List<ArtworkOverview> {
        return fluxDao.getArtworks()
    }

    suspend fun getMovies() : List<Movie> {
        return fluxDao.getMovies()
    }

    suspend fun getMovie(artworkId: Long) : Movie {
        return fluxDao.getMovie(artworkId)
    }

    suspend fun getEpisode(episodeId: Long) : Episode {
        return fluxDao.getEpisode(episodeId)
    }

    suspend fun getEpisodes() : List<Episode> {
        return fluxDao.getEpisodes()
    }

    suspend fun getEpisodes(artworkId: Long) : List<Episode> {
        return fluxDao.getEpisodes(artworkId)
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

//endregion

}