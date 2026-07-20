package com.mskd.flux.features.sources.domain.usecase

import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository

class AddSourceUseCase(
    val repository: SourcesRepository
) {

    suspend operator fun invoke(folder: UserFolder) : Boolean {
        val folders = repository.getFolders()

        val newFolderAlreadyExists = folders.any { folder.path == it.path || folder.path.startsWith("${it.path}/") }
        if (newFolderAlreadyExists) return false

        val alreadyIncludedFolders = folders.filter { it.path.startsWith("${folder.path}/") }
        if (alreadyIncludedFolders.isNotEmpty()) {
            repository.deleteFolders(folders = alreadyIncludedFolders, deleteMedias = false)
        }

        repository.saveFolder(folder = folder)
        return true
    }

}