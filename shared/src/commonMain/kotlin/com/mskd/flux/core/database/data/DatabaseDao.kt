package com.mskd.flux.core.database.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mskd.flux.core.database.data.model.ArtworkEntity
import com.mskd.flux.core.database.data.model.EpisodeEntity
import com.mskd.flux.core.database.data.model.MovieEntity
import com.mskd.flux.core.database.data.model.SeasonEntity
import com.mskd.flux.core.database.data.model.projections.ArtworkImagesProjection
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.sources.data.local.UserFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

//region Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artworks: List<ArtworkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(seasons: List<SeasonEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserFolders(folders: List<UserFolderEntity>)

//endregion

//region Flow

    @Query("SELECT * FROM artworks")
    fun flowArtworks() : Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    fun flowArtwork(artworkId: Long) : Flow<ArtworkEntity?>

    @Query("SELECT * FROM movies WHERE artworkId = :artworkId")
    fun flowMovie(artworkId: Long) : Flow<MovieEntity?>

    @Query("SELECT * FROM episodes WHERE artworkId = :artworkId")
    fun flowEpisodes(artworkId: Long) : Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    fun flowSeasons(artworkId: Long) : Flow<List<SeasonEntity>>

    @Query("SELECT * FROM folders")
    fun flowUserFolders() : Flow<List<UserFolderEntity>>

//endregion

//region Get

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    suspend fun getArtwork(artworkId: Long) : ArtworkEntity?

    @Query("SELECT * FROM artworks")
    suspend fun getArtworks() : List<ArtworkEntity>

    @Query("SELECT * FROM movies WHERE artworkId = :artworkId")
    suspend fun getMovie(artworkId: Long) : MovieEntity?

    @Query("SELECT * FROM movies")
    suspend fun getMovies() : List<MovieEntity>

    @Query("SELECT * FROM movies WHERE name NOT IN (:fileNames)")
    suspend fun getMoviesNotInFiles(fileNames: List<String>) : List<MovieEntity>

    @Query("SELECT * FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodes(artworkId: Long) : List<EpisodeEntity>

    @Query("SELECT * FROM episodes")
    suspend fun getEpisodes() : List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE name NOT IN (:fileNames)")
    suspend fun getEpisodesNotInFiles(fileNames: List<String>) : List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE artworkId = ${Artwork.UNKNOWN_ID}")
    suspend fun getUnknownMedias() : List<EpisodeEntity>

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    suspend fun getSeasons(artworkId: Long) : List<SeasonEntity>

    @Query("SELECT * FROM seasons")
    suspend fun getSeasons() : List<SeasonEntity>

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

    @Query("DELETE FROM movies WHERE path LIKE :folderPath || '%' ESCAPE '\\'")
    suspend fun deleteMoviesInFolder(folderPath: String)

    @Query("DELETE FROM episodes WHERE path LIKE :folderPath || '%' ESCAPE '\\'")
    suspend fun deleteEpisodesInFolder(folderPath: String)

    @Transaction
    suspend fun deleteMediasInFolder(folderPath: String) {
        deleteMoviesInFolder(folderPath = folderPath)
        deleteEpisodesInFolder(folderPath = folderPath)
    }

//endregion

//region Count

    @Query("SELECT COUNT(*) FROM episodes WHERE artworkId = :artworkId")
    suspend fun getEpisodeCountByArtworkId(artworkId: Long): Int

    @Query("SELECT COUNT(*) FROM episodes WHERE artworkId = :artworkId AND season = :season")
    suspend fun getEpisodeCountBySeason(artworkId: Long, season: Int): Int

//endregion

//region Images

    @Query("SELECT imagePath, bannerPath FROM artworks")
    suspend fun getArtworksImages() : List<ArtworkImagesProjection>

    @Query("SELECT imagePath FROM episodes")
    suspend fun getEpisodesImages() : List<String>

    @Query("SELECT imagePath FROM seasons")
    suspend fun getSeasonsImages() : List<String>

//endregion

}