package com.mskd.flux.features.sources.domain.repository

import com.mskd.flux.features.sources.domain.validator.UserFolderValidator
import com.mskd.flux.features.sources.data.datasource.SourcesDataSource
import com.mskd.flux.features.sources.data.mapper.toDomain
import com.mskd.flux.features.sources.data.mapper.toEntity
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.features.sources.domain.model.UserFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourcesRepositoryImpl(
    private val dataSource: SourcesDataSource,
    private val userFolderValidator: UserFolderValidator
) : SourcesRepository {

    override fun flowFolders(): Flow<List<UserFolder>> {
        return dataSource.flowFolders()
            .map { entities ->
                entities.map { entity ->
                    val folder = entity.toDomain()
                    if (folder.source == FileSource.LOCAL) {
                        val currentStatus = userFolderValidator.isFolderAvailable(folder.path)
                        folder.copy(status = currentStatus)
                    } else {
                        folder
                    }
                }
            }
    }

    override suspend fun getFolders(): List<UserFolder> {
        return dataSource.getFolders()
            .map { entity ->
                val folder = entity.toDomain()
                if (folder.source == FileSource.LOCAL) {
                    val currentStatus = userFolderValidator.isFolderAvailable(folder.path)
                    folder.copy(status = currentStatus)
                } else {
                    folder
                }
            }
    }

    override suspend fun saveFolder(folder: UserFolder) {
        dataSource.saveFolder(folder = folder.toEntity())
    }

    override suspend fun deleteFolder(folder: UserFolder) {
        dataSource.deleteFolder(folder = folder.toEntity())
    }

    override suspend fun deleteFolders(folders: List<UserFolder>) {
        dataSource.deleteFolders(folders = folders.map { it.toEntity() })
    }

}