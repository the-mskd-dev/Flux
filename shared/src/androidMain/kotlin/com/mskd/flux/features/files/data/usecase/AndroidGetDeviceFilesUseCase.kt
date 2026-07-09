package com.mskd.flux.features.files.data.usecase

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class AndroidGetDeviceFilesUseCase(
    private val mediaStore: FilesDataSource,
    private val saf: FilesDataSource
) : GetDeviceFilesUseCase {
    override suspend fun invoke(): List<UserFile> {
        return coroutineScope {

            val mediaStoreFiles = async { mediaStore.getFiles() }
            val safFiles = async { saf.getFiles() }

            mediaStoreFiles.await() + safFiles.await()

        }
    }
}