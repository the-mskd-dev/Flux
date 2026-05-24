package com.mskd.flux.data.ddb

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.model.dto.ArtworkImagesDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

//region Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artworks: List<Artwork>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<Episode>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(seasons: List<Season>)

//endregion

//region Flow

    @Query("SELECT * FROM artworks")
    fun flowArtworks() : Flow<List<Artwork>>

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    fun flowArtwork(artworkId: Long) : Flow<Artwork?>

    @Query("SELECT * FROM movies WHERE artworkId = :artworkId")
    fun flowMovie(artworkId: Long) : Flow<Movie?>

    @Query("SELECT * FROM episodes WHERE artworkId = :artworkId")
    fun flowEpisodes(artworkId: Long) : Flow<List<Episode>>

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    fun flowSeasons(artworkId: Long) : Flow<List<Season>>

//endregion

//region Get

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    suspend fun getArtwork(artworkId: Long) : Artwork?

    @Query("SELECT * FROM artworks")
    suspend fun getArtworks() : List<Artwork>

    @Query("SELECT * FROM movies WHERE artworkId = :artworkId")
    suspend fun getMovie(artworkId: Long) : Movie?

    @Query("SELECT * FROM movies")
    suspend fun getMovies() : List<Movie>

    @Query("SELECT * FROM movies WHERE name NOT IN (:fileNames)")
    suspend fun getMoviesNotInFiles(fileNames: List<String>) : List<Movie>

    @Query("SELECT * FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodes(artworkId: Long) : List<Episode>

    @Query("SELECT * FROM episodes")
    suspend fun getEpisodes() : List<Episode>

    @Query("SELECT * FROM episodes WHERE name NOT IN (:fileNames)")
    suspend fun getEpisodesNotInFiles(fileNames: List<String>) : List<Episode>

    @Query("SELECT * FROM episodes WHERE artworkId = ${Artwork.UNKNOWN_ID}")
    suspend fun getUnknownMedias() : List<Episode>

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    suspend fun getSeasons(artworkId: Long) : List<Season>

    @Query("SELECT * FROM seasons")
    suspend fun getSeasons() : List<Season>

//endregion

//region Delete

    @Query("DELETE FROM artworks WHERE id IN (:ids)")
    suspend fun deleteArtworks(ids: List<Long>)

    @Query("""
        DELETE FROM artworks
        WHERE id NOT IN (
            SELECT DISTINCT artworkId FROM episodes
            UNION
            SELECT DISTINCT artworkId FROM movies
        )
    """)
    suspend fun deleteEmptyArtworks()

    @Query("DELETE FROM movies WHERE artworkId IN (:ids)")
    suspend fun deleteMoviesByIds(ids: List<Long>)

    @Query("DELETE FROM episodes WHERE id IN (:ids)")
    suspend fun deleteEpisodesByIds(ids: List<Long>)

    @Query("DELETE FROM episodes WHERE artworkId = :artworkId")
    suspend fun deleteEpisodesByArtworkId(artworkId: Long)

    @Query("DELETE FROM seasons WHERE artworkId IN (:artworkIds)")
    suspend fun deleteSeasonsByIds(artworkIds: List<Long>)

    @Query("""
    DELETE FROM seasons
    WHERE NOT EXISTS (
        SELECT 1 FROM episodes
        WHERE episodes.artworkId = seasons.artworkId
        AND episodes.season = seasons.season
    )
""")
    suspend fun deleteEmptySeasons()

    @Query("DELETE FROM seasons WHERE artworkId = :artworkId AND season = :season")
    suspend fun deleteSeason(artworkId: Long, season: Int)

    @Query("DELETE FROM artworks")
    suspend fun deleteAllArtworks()

    @Query("DELETE FROM movies")
    suspend fun deleteAllMovies()

    @Query("DELETE FROM episodes")
    suspend fun deleteAllEpisodes()

    @Query("DELETE FROM seasons")
    suspend fun deleteAllSeasons()

//endregion

//region Count

    @Query("SELECT COUNT(*) FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodeCountByArtworkId(artworkId: Long): Int

    @Query("SELECT COUNT(*) FROM episodes WHERE artworkId = :artworkId AND season = :season")
    suspend fun getEpisodeCountBySeason(artworkId: Long, season: Int): Int

//endregion

//region Images

    @Query("SELECT imagePath, bannerPath FROM artworks")
    suspend fun getArtworksImages() : List<ArtworkImagesDTO>

    @Query("SELECT imagePath FROM episodes")
    suspend fun getEpisodesImages() : List<String>

    @Query("SELECT imagePath FROM seasons")
    suspend fun getSeasonsImages() : List<String>

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

@Database(
    entities = [Artwork::class, Movie::class, Episode::class, Season::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ]
)
@TypeConverters(Converters::class)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun dao(): DatabaseDao

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