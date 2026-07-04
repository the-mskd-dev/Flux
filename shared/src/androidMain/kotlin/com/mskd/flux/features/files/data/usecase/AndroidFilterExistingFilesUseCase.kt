package com.mskd.flux.features.files.data.usecase

import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase

class AndroidFilterExistingFilesUseCase(
    private val mediaStore: FilesRepository,
    private val saf: FilesRepository
) : FilterExistingFilesUseCase {
    override suspend fun invoke(files: List<UserFile>): List<UserFile> {
        val mediaStoreFiles = mediaStore.filterExistingFiles(files = files)
        val safFiles = saf.filterExistingFiles(files = files)
        return mediaStoreFiles + safFiles
    }
}