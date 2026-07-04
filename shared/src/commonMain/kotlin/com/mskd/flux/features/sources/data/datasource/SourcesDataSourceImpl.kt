package com.mskd.flux.features.sources.data.datasource

import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.features.sources.data.local.UserFolderEntity
import kotlinx.coroutines.flow.Flow

class SourcesDataSourceImpl(private val dao: SourcesDao) : SourcesDataSource {

    override fun flowFolders(): Flow<List<UserFolderEntity>> {
        return dao.flowFolders()
    }

    override suspend fun saveFolder(folder: UserFolderEntity) {
        dao.insertFolder(folder = folder)
    }

    override suspend fun getFolders(): List<UserFolderEntity> {
        return dao.getFolders()
    }

    override suspend fun deleteFolder(folder: UserFolderEntity) {
        dao.deleteFolder(path = folder.path)
    }

    override suspend fun deleteFolders(folders: List<UserFolderEntity>) {
        dao.deleteFolders(paths = folders.map { it.path })
    }

}