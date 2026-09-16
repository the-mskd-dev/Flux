package com.mskd.flux.core.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.core.database.data.model.ArtworkEntity
import com.mskd.flux.core.database.data.model.projections.ArtworkImagesProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtworkDao {

//region Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtworks(artworks: List<ArtworkEntity>)

//endregion

//region Flow

    @Query("SELECT * FROM artworks WHERE (:includePrivates = 1 OR isPrivate = 0)")
    fun flowArtworks(includePrivates: Boolean = false) : Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks WHERE isPrivate = 1")
    fun flowPrivateArtworks() : Flow<List<ArtworkEntity>>

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    fun flowArtwork(artworkId: Long) : Flow<ArtworkEntity?>

//endregion

//region Get

    @Query("SELECT * FROM artworks WHERE id = :artworkId")
    suspend fun getArtwork(artworkId: Long) : ArtworkEntity?

    @Query("SELECT * FROM artworks WHERE (:includePrivates = 1 OR isPrivate = 0)")
    suspend fun getArtworks(includePrivates: Boolean = false) : List<ArtworkEntity>

//endregion

//region Private

    @Query("UPDATE artworks SET isPrivate = :isPrivate WHERE id = :artworkId")
    suspend fun setArtworkPrivate(artworkId: Long, isPrivate: Boolean)

    @Query("SELECT id FROM artworks WHERE isPrivate = 1")
    suspend fun getPrivateArtworkIds() : List<Long>

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