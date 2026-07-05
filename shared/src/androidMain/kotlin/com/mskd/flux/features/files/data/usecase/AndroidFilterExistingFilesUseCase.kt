package com.mskd.flux.features.files.data.usecase

import com.mskd.flux.core.domain.model.files.FileSource
import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class AndroidFilterExistingFilesUseCase(
    private val mediaStore: FilesRepository,
    private val saf: FilesRepository
) : FilterExistingFilesUseCase {
    override suspend fun invoke(files: List<UserFile>): List<UserFile> {

        val availableFiles = supervisorScope {

            val mediaStoreDeferred = async { mediaStore.filterExistingFiles(files = files) }

            val safDeferred = async { saf.filterExistingFiles(files = files) }

            mediaStoreDeferred.await() + safDeferred.await()

        }

        return availableFiles
    }
}