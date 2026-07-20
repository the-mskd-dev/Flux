package com.mskd.flux.features.sources.data.repository

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.features.sources.data.local.UserFolderEntity
import com.mskd.flux.features.sources.data.mapper.toDomain
import com.mskd.flux.features.sources.data.mapper.toEntity
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourcesRepositoryImpl(
    private val dao: SourcesDao,
    private val databaseRepository: DatabaseRepository,
    private val userFolderValidator: UserFolderValidator
) : SourcesRepository {

    override fun flowFolders(): Flow<List<UserFolder>> {
        return dao.flowFolders().map { entities -> entities.map { it.toDomainWithStatus() } }
    }

    override suspend fun getFolders(): List<UserFolder> {
        return dao.getFolders().map { it.toDomainWithStatus() }
    }

    override suspend fun saveFolder(folder: UserFolder) {
        dao.insertFolder(folder = folder.toEntity())
    }

    override suspend fun deleteFolder(folder: UserFolder, deleteMedias: Boolean) {
        dao.deleteFolder(path = folder.path)

        if (deleteMedias) {
            databaseRepository.deleteMediasInFolder(folder = folder)
        }

    }

    override suspend fun deleteFolders(folders: List<UserFolder>, deleteMedias: Boolean) {
        dao.deleteFolders(paths = folders.map { it.path })
        if (deleteMedias) {
            folders.forEach { folder -> databaseRepository.deleteMediasInFolder(folder = folder) }
        }
    }

    private suspend fun UserFolderEntity.toDomainWithStatus(): UserFolder {
        val status = if (source != FileSource.LOCAL)
            userFolderValidator.isFolderAvailable(path)
        else
            true

        return this.toDomain(isAvailable = status)
    }

}