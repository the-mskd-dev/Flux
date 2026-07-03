package com.mskd.flux.features.files.domain.useCase

import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.model.domain.files.UserFile

class AndroidGetFilesUseCase(
    private val mediaStore: FilesRepository,
    private val saf: FilesRepository
) : GetFilesUseCase {
    override suspend fun invoke(): List<UserFile> {
        return mediaStore.getFiles() + saf.getFiles()
    }
}