package com.mskd.flux.data.repository.ddb.sources

import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.flow.Flow

interface SourcesRepository {

    fun flowFolders() : Flow<List<UserFolder>>
    suspend fun saveFolder(folder: UserFolder)
    suspend fun getFolders() : List<UserFolder>
    suspend fun deleteFolder(folder: UserFolder)
    suspend fun deleteFolders(folders: List<UserFolder>)



}