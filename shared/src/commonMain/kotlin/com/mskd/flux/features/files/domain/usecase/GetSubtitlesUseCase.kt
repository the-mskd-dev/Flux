package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile

interface GetSubtitlesUseCase {
    suspend operator fun invoke(file: UserFile) : String?
}