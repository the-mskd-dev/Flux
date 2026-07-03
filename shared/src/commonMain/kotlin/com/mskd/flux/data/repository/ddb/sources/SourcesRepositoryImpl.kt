package com.mskd.flux.data.repository.ddb.sources

import com.mskd.flux.data.local.ddb.SourcesDao
import com.mskd.flux.model.data.local.mappers.toDomain
import com.mskd.flux.model.data.local.mappers.toEntity
import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourcesRepositoryImpl(private val dao: SourcesDao) : SourcesRepository {

    override fun flowFolders(): Flow<List<UserFolder>> {
        return dao.flowFolders().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun saveFolder(folder: UserFolder) {
        dao.insertFolder(folder = folder.toEntity())
    }

    override suspend fun getFolders(): List<UserFolder> {
        return dao.getFolders().map { it.toDomain() }
    }

    override suspend fun deleteFolder(folder: UserFolder) {
        dao.deleteFolder(path = folder.path)
    }

    override suspend fun deleteFolders(folders: List<UserFolder>) {
        dao.deleteFolders(paths = folders.map { it.path })
    }

}