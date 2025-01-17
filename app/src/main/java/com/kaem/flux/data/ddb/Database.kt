package com.kaem.flux.data.ddb

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.coroutineScope

@Dao
interface FluxDao {

//region Insert

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOverviews(artworkOverviews: List<ArtworkOverview>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMovies(movies: List<Movie>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEpisodes(episodes: List<Episode>)

//endregion

//region Get

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    suspend fun getOverview(artworkId: Long) : ArtworkOverview?

    @Query("SELECT * FROM artworks")
    suspend fun getOverviews() : List<ArtworkOverview>

    @Query("SELECT * FROM movies WHERE artworkId = :artworkId")
    suspend fun getMovie(artworkId: Long) : Movie?

    @Query("SELECT * FROM movies")
    suspend fun getMovies() : List<Movie>

    @Query("SELECT * FROM movies WHERE name NOT IN (:fileNames)")
    suspend fun getMoviesWithNoFiles(fileNames: List<String>) : List<Movie>

    @Query("SELECT name FROM movies")
    suspend fun getMoviesFileNames(): List<String>

    @Query("SELECT * FROM episodes WHERE id = :episodeId")
    suspend fun getEpisode(episodeId: Long) : Episode?

    @Query("SELECT * FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodes(artworkId: Long) : List<Episode>

    @Query("SELECT * FROM episodes")
    suspend fun getEpisodes() : List<Episode>

    @Query("SELECT * FROM episodes WHERE name NOT IN (:fileNames)")
    suspend fun getEpisodesWithNoFiles(fileNames: List<String>) : List<Episode>

    @Query("SELECT name FROM episodes")
    suspend fun getEpisodesFileNames(): List<String>

    suspend fun getAllFileNames() : List<String> {
        val movieFileNames = getMoviesFileNames()
        val episodeFileNames = getEpisodesFileNames()
        return movieFileNames + episodeFileNames
    }

//endregion

//region Delete

    @Query("DELETE FROM artworks WHERE id IN (:ids)")
    suspend fun deleteOverviews(ids: List<Long>)

    @Query("DELETE FROM movies WHERE artworkId IN (:ids)")
    suspend fun deleteMoviesByIds(ids: List<Long>)

    @Query("DELETE FROM episodes WHERE id IN (:ids)")
    suspend fun deleteEpisodesByIds(ids: List<Long>)

    @Transaction
    suspend fun deleteMovies(movies: List<Movie>) {

        // Delete overviews, it will also delete related movies
        deleteOverviews(movies.map { it.artworkId })

    }

    @Transaction
    suspend fun deleteEpisodes(episodes: List<Episode>) {

        // Delete episodes
        deleteEpisodesByIds(episodes.map { it.id })

        // Delete overviews if needed
        episodes
            .map { it.artworkId }
            .distinct()
            .forEach { artworkId ->

                // Check if it remains episode for show
                val remainingEpisodes = getEpisodeCountByOverviewId(artworkId)

                // If no, delete the show
                if (remainingEpisodes == 0) {
                    deleteOverviews(listOf(artworkId))
                }

            }

    }

    @Transaction
    suspend fun deleteArtworksWithNoFiles(existingFiles: List<UserFile>) {

        val moviesToDelete = getMoviesWithNoFiles(fileNames = existingFiles.map { it.name })
        val episodesToDelete = getEpisodesWithNoFiles(fileNames = existingFiles.map { it.name })

        deleteMovies(moviesToDelete)
        deleteEpisodes(episodesToDelete)

    }

//endregion

//region Count

    @Query("SELECT COUNT(*) FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodeCountByOverviewId(artworkId: Long): Int

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

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: FluxDatabase? = null

        fun getInstance(context: Context): FluxDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): FluxDatabase {
            return Room
                .databaseBuilder(
                    context,
                    FluxDatabase::class.java,
                    "fluxDatabase"
                )
                .build()
        }
    }
}