package com.mskd.flux.features.sources.data.dataSource

import com.mskd.flux.features.sources.data.model.UserFolderEntity
import kotlinx.coroutines.flow.Flow

interface SourcesDataSource {
    fun flowFolders() : Flow<List<UserFolderEntity>>
    suspend fun saveFolder(folder: UserFolderEntity)
    suspend fun getFolders() : List<UserFolderEntity>
    suspend fun deleteFolder(folder: UserFolderEntity)
    suspend fun deleteFolders(folders: List<UserFolderEntity>)
}