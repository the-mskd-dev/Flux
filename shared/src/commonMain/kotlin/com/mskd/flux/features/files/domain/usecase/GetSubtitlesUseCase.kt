package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.model.ExternalSubtitle
import com.mskd.flux.features.sources.domain.provider.SourcesProvider

interface GetSubtitlesUseCase {
    suspend operator fun invoke(file: UserFile) : List<ExternalSubtitle>
}

class GetSubtitlesUseCaseImpl(
    private val sourcesProvider: SourcesProvider
) : GetSubtitlesUseCase {

    override suspend fun invoke(file: UserFile): List<ExternalSubtitle> {
        val source = sourcesProvider.getSources().firstOrNull { it.contains(file) } ?: return emptyList()
        return source.getSubtitlesFor(file = file)
    }

}