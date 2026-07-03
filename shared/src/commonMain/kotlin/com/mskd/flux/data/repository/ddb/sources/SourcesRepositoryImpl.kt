package com.mskd.flux.data.repository.ddb.sources

import com.mskd.flux.data.repository.sources.CheckFolderAvailabilityDataSource
import com.mskd.flux.data.local.ddb.SourcesDao
import com.mskd.flux.model.data.local.mappers.toDomain
import com.mskd.flux.model.data.local.mappers.toEntity
import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.model.domain.files.UserFolder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SourcesRepositoryImpl(
    private val dao: SourcesDao,
    private val checkFolderAvailabilityDataSource: CheckFolderAvailabilityDataSource
) : SourcesRepository {

    override fun flowFolders(): Flow<List<UserFolder>> {
        return dao.flowFolders()
            .map { entities ->
                entities.map { entity ->
                    val folder = entity.toDomain()
                    if (folder.source == FileSource.LOCAL) {
                        val currentStatus = checkFolderAvailabilityDataSource.isFolderAvailable(folder.path)
                        folder.copy(status = currentStatus)
                    } else {
                        folder
                    }
                }
            }
    }

    override suspend fun getFolders(): List<UserFolder> {
        return dao.getFolders()
            .map { entity ->
                val folder = entity.toDomain()
                if (folder.source == FileSource.LOCAL) {
                    val currentStatus = checkFolderAvailabilityDataSource.isFolderAvailable(folder.path)
                    folder.copy(status = currentStatus)
                } else {
                    folder
                }
            }
    }

    override suspend fun saveFolder(folder: UserFolder) {
        dao.insertFolder(folder = folder.toEntity())
    }

    override suspend fun deleteFolder(folder: UserFolder) {
        dao.deleteFolder(path = folder.path)
    }

    override suspend fun deleteFolders(folders: List<UserFolder>) {
        dao.deleteFolders(paths = folders.map { it.path })
    }

}