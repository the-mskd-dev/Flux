package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.sources.domain.provider.SourcesProvider

interface GetSubtitlesUseCase {
    suspend operator fun invoke(file: UserFile) : String?
}

class GetSubtitlesUseCaseImpl(
    private val sources: Map<FileSource, FilesDataSource>,
    private val sourcesProvider: SourcesProvider
) : GetSubtitlesUseCase {

    override suspend fun invoke(file: UserFile): String? {
        val dataSource = sources[file.source] ?: return null

        val activeSources = sourcesProvider.getSources()
        if (dataSource !in activeSources) return null

        return dataSource.getSubtitlesFor(file = file)
    }

}