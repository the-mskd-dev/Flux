package com.mskd.flux.features.credits.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.features.credits.data.model.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM person WHERE artworkId = :artworkId")
    fun flowPerson(artworkId: Long): Flow<List<PersonEntity>>

    @Query("SELECT DISTINCT artworkId FROM person WHERE id = :personId")
    suspend fun getArtworkIdsForPerson(personId: Int): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(people: List<PersonEntity>)

    @Query("DELETE FROM person WHERE artworkId = :artworkId")
    suspend fun delete(artworkId: Long)

}