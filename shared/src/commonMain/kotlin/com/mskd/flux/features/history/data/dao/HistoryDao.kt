package com.mskd.flux.features.history.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.history.data.model.HistoryProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("""
        SELECT * FROM history
        INNER JOIN medias ON history.mediaId = medias.id AND history.historyArtworkId = medias.artworkId
        ORDER BY history.timestamp DESC
    """)
    fun flow() : Flow<List<HistoryProjection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: HistoryEntity)

    @Query("DELETE FROM history WHERE historyArtworkId = :artworkId")
    suspend fun delete(artworkId: Long)

    @Query("DELETE FROM history")
    suspend fun clear()

}