package com.mskd.flux.features.sources.domain.useCase

import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.model.UserFolder

class AddSourceUseCase(
    val repository: SourcesRepository
) {

    suspend operator fun invoke(folder: UserFolder) : Boolean {
        val folders = repository.getFolders()

        val newFolderAlreadyExists = folders.any { folder.path.startsWith(it.path) }
        if (newFolderAlreadyExists) return false

        val alreadyIncludedFolders = folders.filter { it.path.startsWith(folder.path) }
        if (alreadyIncludedFolders.isNotEmpty()) {
            repository.deleteFolders(folders = alreadyIncludedFolders)
        }

        repository.saveFolder(folder = folder)
        return true
    }

}