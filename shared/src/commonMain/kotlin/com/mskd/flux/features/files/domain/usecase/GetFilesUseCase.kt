package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.domain.model.files.UserFile

interface GetFilesUseCase {
    suspend operator fun invoke() : List<UserFile>
}