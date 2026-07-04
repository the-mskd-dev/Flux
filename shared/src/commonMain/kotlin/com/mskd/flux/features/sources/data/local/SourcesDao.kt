package com.mskd.flux.features.sources.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SourcesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: UserFolderEntity)

    @Query("SELECT * FROM folders")
    fun flowFolders() : Flow<List<UserFolderEntity>>

    @Query("SELECT * FROM folders")
    suspend fun getFolders() : List<UserFolderEntity>

    @Query("DELETE FROM folders WHERE path = :path")
    suspend fun deleteFolder(path: String)

    @Query("DELETE FROM folders WHERE path IN (:paths)")
    suspend fun deleteFolders(paths: List<String>)

}