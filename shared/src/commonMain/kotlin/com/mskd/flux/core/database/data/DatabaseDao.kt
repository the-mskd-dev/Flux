package com.mskd.flux.core.database.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mskd.flux.core.database.data.model.ArtworkEntity
import com.mskd.flux.core.database.data.model.MediaEntity
import com.mskd.flux.core.database.data.model.SeasonEntity
import com.mskd.flux.core.database.data.model.projections.ArtworkImagesProjection
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.files.UserFile
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

//region Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artworks: List<ArtworkEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedias(medias: List<MediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeasons(seasons: List<SeasonEntity>)

//endregion

//region Flow

    @Query("SELECT * FROM artworks")
    fun flowArtworks() : Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    fun flowArtwork(artworkId: Long) : Flow<ArtworkEntity?>

    @Query("SELECT * FROM medias WHERE artworkId = :artworkId")
    fun flowMedias(artworkId: Long) : Flow<List<MediaEntity>>

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    fun flowSeasons(artworkId: Long) : Flow<List<SeasonEntity>>

//endregion

//region Get

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    suspend fun getArtwork(artworkId: Long) : ArtworkEntity?

    @Query("SELECT * FROM artworks")
    suspend fun getArtworks() : List<ArtworkEntity>

    @Query("SELECT * FROM medias WHERE artworkId = :artworkId")
    suspend fun getMedias(artworkId: Long) : List<MediaEntity>

    @Query("SELECT * FROM medias")
    suspend fun getMedias() : List<MediaEntity>

    @Query("SELECT * FROM medias WHERE name NOT IN (:fileNames)")
    suspend fun getMediasNotInFiles(fileNames: List<String>) : List<MediaEntity>

    @Query("SELECT * FROM medias WHERE artworkId = ${Artwork.UNKNOWN_ID}")
    suspend fun getUnknownMedias() : List<MediaEntity>

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    suspend fun getSeasons(artworkId: Long) : List<SeasonEntity>

    @Query("SELECT * FROM seasons")
    suspend fun getSeasons() : List<SeasonEntity>

//endregion

//region Update

    @Transaction
    suspend fun updateRealPaths(files: List<UserFile>) {
        files.forEach { file ->
            updateRealPathIfEmpty(path = file.path, realPath = file.realPath)
        }
    }

    @Query("UPDATE medias SET realPath = :realPath WHERE path = :path AND realPath = ''")
    suspend fun updateRealPathIfEmpty(path: String, realPath: String)

//endregion

//region Delete

    @Query("DELETE FROM artworks WHERE id IN (:artworkIds)")
    suspend fun deleteArtworks(artworkIds: List<Long>)

    @Query("""
        DELETE FROM artworks
        WHERE id NOT IN (
            SELECT DISTINCT artworkId FROM medias
        )
    """)
    suspend fun deleteEmptyArtworks()

    @Query("DELETE FROM medias WHERE artworkId IN (:artworkIds)")
    suspend fun deleteMediasByArtworkIds(artworkIds: List<Long>)

    @Query("DELETE FROM medias WHERE id IN (:ids) AND type = :type")
    suspend fun deleteEpisodesByIds(ids: List<Long>, type: ContentType = ContentType.SHOW)

    @Query("DELETE FROM seasons WHERE artworkId IN (:artworkIds)")
    suspend fun deleteSeasonsByArtworkIds(artworkIds: List<Long>)

    @Query("""
    DELETE FROM seasons
    WHERE NOT EXISTS (
        SELECT 1 FROM medias
        WHERE medias.artworkId = seasons.artworkId
        AND medias.season = seasons.season
        AND medias.type = :type
    )
""")
    suspend fun deleteEmptySeasons(type: ContentType = ContentType.SHOW)

    @Query("DELETE FROM seasons WHERE artworkId = :artworkId AND season = :season")
    suspend fun deleteSeason(artworkId: Long, season: Int)

    @Query("DELETE FROM artworks")
    suspend fun deleteAllArtworks()

    @Query("DELETE FROM medias")
    suspend fun deleteAllMedias()

    @Query("DELETE FROM seasons")
    suspend fun deleteAllSeasons()

    @Query("DELETE FROM medias WHERE path LIKE :folderPath || '%' ESCAPE '\\'")
    suspend fun deleteMediasInFolder(folderPath: String)

//endregion

//region Count

    @Query("SELECT COUNT(*) FROM medias WHERE artworkId = :artworkId AND type = :type")
    suspend fun getEpisodeCountByArtworkId(artworkId: Long, type: ContentType = ContentType.SHOW): Int

    @Query("SELECT COUNT(*) FROM medias WHERE artworkId = :artworkId AND season = :season AND type = :type")
    suspend fun getEpisodeCountBySeason(artworkId: Long, season: Int, type: ContentType = ContentType.SHOW): Int

//endregion

//region Images

    @Query("SELECT imagePath, bannerPath FROM artworks")
    suspend fun getArtworksImages() : List<ArtworkImagesProjection>

    @Query("SELECT imagePath FROM medias WHERE imagePath IS NOT NULL")
    suspend fun getMediasImages() : List<String>

    @Query("SELECT imagePath FROM seasons")
    suspend fun getSeasonsImages() : List<String>

//endregion

}