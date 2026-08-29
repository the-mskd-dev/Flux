package com.mskd.flux.core.database.data.dao

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

//endregion

//region Flow

    @Query("SELECT * FROM artworks")
    fun flowArtworks() : Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    fun flowArtwork(artworkId: Long) : Flow<ArtworkEntity?>

//endregion

//region Get

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    suspend fun getArtwork(artworkId: Long) : ArtworkEntity?

    @Query("SELECT * FROM artworks")
    suspend fun getArtworks() : List<ArtworkEntity>

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

    @Query("DELETE FROM artworks")
    suspend fun deleteAllArtworks()

//endregion

//region Images

    @Query("SELECT imagePath, bannerPath FROM artworks")
    suspend fun getArtworksImages() : List<ArtworkImagesProjection>

//endregion

}