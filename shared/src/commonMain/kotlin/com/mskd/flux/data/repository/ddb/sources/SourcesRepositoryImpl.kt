package com.mskd.flux.data.repository.ddb.sources

import com.mskd.flux.data.local.ddb.SourcesDao
import com.mskd.flux.model.data.local.mappers.toDomain
import com.mskd.flux.model.data.local.mappers.toEntity
import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourcesRepositoryImpl(private val dao: SourcesDao) : SourcesRepository {

    override fun flowUserFolders(): Flow<List<UserFolder>> {
        return dao.flowUserFolders().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveUserFolders(folders: List<UserFolder>) {
        dao.insertUserFolders(folders = folders.map { it.toEntity() })
    }

    override suspend fun getUserFolders(): List<UserFolder> {
        return dao.getUserFolders().map { it.toDomain() }
    }

    override suspend fun deleteUserFolder(userFolder: UserFolder) {
        dao.deleteUserFolder(path = userFolder.path)
    }

}