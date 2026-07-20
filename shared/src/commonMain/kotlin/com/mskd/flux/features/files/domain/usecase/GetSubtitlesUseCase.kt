package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource

interface GetSubtitlesUseCase {
    suspend operator fun invoke(file: UserFile) : String?
}

class GetSubtitlesUseCaseImpl(
    private val sources: Map<FileSource, FilesDataSource>
) : GetSubtitlesUseCase {
    override suspend fun invoke(file: UserFile): String? =
        sources[file.source]?.getSubtitlesFor(file = file)
}