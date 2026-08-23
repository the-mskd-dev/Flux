package com.mskd.flux.features.history.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.core.database.data.model.GenreEntity
import com.mskd.flux.features.history.data.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history")
    fun flow() : Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entries: List<HistoryEntity>)

    @Query("DELETE FROM history WHERE artworkId = :artworkId")
    suspend fun delete(artworkId: Long)

    @Query("DELETE FROM history")
    suspend fun clear()

}