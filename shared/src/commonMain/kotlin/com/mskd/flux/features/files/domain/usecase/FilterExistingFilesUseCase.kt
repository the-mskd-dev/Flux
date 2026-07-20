package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface FilterExistingFilesUseCase {
    suspend operator fun invoke(files: List<UserFile>) : List<UserFile>
}

class FilterExistingFilesUseCaseImpl(
    private val sources: List<FilesDataSource>,
) : FilterExistingFilesUseCase {
    override suspend fun invoke(files: List<UserFile>): List<UserFile> = coroutineScope {
        sources
            .map { source -> async { source.filterExistingFiles(files = files) } }
            .awaitAll()
            .flatten()
    }
}