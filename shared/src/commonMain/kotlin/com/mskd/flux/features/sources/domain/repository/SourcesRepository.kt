package com.mskd.flux.features.sources.domain.repository

import com.mskd.flux.features.sources.domain.model.UserFolder
import kotlinx.coroutines.flow.Flow

interface SourcesRepository {

    fun flowFolders() : Flow<List<UserFolder>>
    suspend fun saveFolder(folder: UserFolder)
    suspend fun getFolders() : List<UserFolder>
    suspend fun deleteFolder(folder: UserFolder, deleteMedias: Boolean)
    suspend fun deleteFolders(folders: List<UserFolder>, deleteMedias: Boolean)



}