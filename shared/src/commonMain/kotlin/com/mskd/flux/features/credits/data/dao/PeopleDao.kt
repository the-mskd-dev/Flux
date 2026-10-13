package com.mskd.flux.features.credits.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.features.credits.data.model.PeopleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeopleDao {

    @Query("SELECT * FROM people WHERE artworkId = :artworkId")
    fun flowPeople(artworkId: Long): Flow<List<PeopleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(people: List<PeopleEntity>)

    @Query("DELETE FROM people WHERE artworkId = :artworkId")
    suspend fun delete(artworkId: Long)

}