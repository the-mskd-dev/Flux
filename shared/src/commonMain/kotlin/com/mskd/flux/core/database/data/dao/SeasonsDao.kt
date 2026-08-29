package com.mskd.flux.core.database.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.core.database.data.model.SeasonEntity
import com.mskd.flux.core.model.artwork.ContentType
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonsDao {

    //region Insert

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(seasons: List<SeasonEntity>)

    //endregion

    //region Flow

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    fun flow(artworkId: Long) : Flow<List<SeasonEntity>>

    //endregion

    //region Select

    @Query("SELECT * FROM seasons WHERE artworkId = :artworkId")
    suspend fun getForArtwork(artworkId: Long) : List<SeasonEntity>

    @Query("SELECT * FROM seasons")
    suspend fun getAll() : List<SeasonEntity>

    @Query("SELECT imagePath FROM seasons")
    suspend fun getImages() : List<String>

    //endregion

    //region Delete

    @Query("DELETE FROM seasons WHERE artworkId IN (:artworkIds)")
    suspend fun deleteByArtworkIds(artworkIds: List<Long>)

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
    suspend fun delete(artworkId: Long, season: Int)

    @Query("DELETE FROM seasons")
    suspend fun deleteAll()

    //endregion

}