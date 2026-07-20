package com.mskd.flux.features.sources.fake

import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeSourcesRepository(initFolders: List<UserFolder> = emptyList()) : SourcesRepository {

    private val _flow = MutableStateFlow(initFolders)

    override fun flowFolders(): Flow<List<UserFolder>> {
        return _flow.asStateFlow()
    }

    override suspend fun saveFolder(folder: UserFolder) {
        _flow.update { it + folder }
    }

    override suspend fun getFolders(): List<UserFolder> {
        return _flow.value
    }

    override suspend fun deleteFolder(folder: UserFolder, deleteMedias: Boolean) {
        _flow.update { state ->
            val folders = state.toMutableList()
            folders.removeIf { it.path == folder.path }
            folders
        }
    }

    override suspend fun deleteFolders(folders: List<UserFolder>, deleteMedias: Boolean) {
        _flow.update { state ->
            val tmp = state.toMutableList()
            folders.forEach { folder ->
                tmp.removeIf { it.path == folder.path }
            }
            tmp
        }
    }

}