package com.mskd.flux.features.files.data.usecase

import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import java.io.File

class AndroidGetSubtitlesUseCase(
    private val mediaStore: FilesRepository,
    private val saf: FilesRepository
) : GetSubtitlesUseCase {
    override suspend fun invoke(file: UserFile): String? {
        return mediaStore.getSubtitlesFor(file = file) ?: saf.getSubtitlesFor(file = file)
    }
}