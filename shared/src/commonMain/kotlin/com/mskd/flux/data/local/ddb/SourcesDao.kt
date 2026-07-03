package com.mskd.flux.data.local.ddb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mskd.flux.model.data.local.entities.UserFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourcesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserFolders(folders: List<UserFolderEntity>)

    @Query("SELECT * FROM folders")
    fun flowUserFolders() : Flow<List<UserFolderEntity>>

    @Query("SELECT * FROM folders")
    suspend fun getUserFolders() : List<UserFolderEntity>

    @Query("DELETE FROM folders WHERE path = :path")
    suspend fun deleteUserFolder(path: String)

}