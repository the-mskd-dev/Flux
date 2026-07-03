package com.mskd.flux.data.repository.ddb.sources

import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.flow.Flow

interface SourcesRepository {

    fun flowUserFolders() : Flow<List<UserFolder>>

    suspend fun saveUserFolders(folders: List<UserFolder>)

    suspend fun getUserFolders() : List<UserFolder>

    suspend fun deleteUserFolder(userFolder: UserFolder)

}