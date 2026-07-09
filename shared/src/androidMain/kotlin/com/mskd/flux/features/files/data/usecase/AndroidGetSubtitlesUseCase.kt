package com.mskd.flux.features.files.data.usecase

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase

class AndroidGetSubtitlesUseCase(
    private val mediaStore: FilesDataSource,
    private val saf: FilesDataSource
) : GetSubtitlesUseCase {
    override suspend fun invoke(file: UserFile): String? {
        return when (file.source) {
            FileSource.LOCAL -> mediaStore.getSubtitlesFor(file = file)
            FileSource.SAF -> saf.getSubtitlesFor(file = file)
        }
    }
}