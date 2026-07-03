package com.mskd.flux.features.files.domain.useCase

import com.mskd.flux.model.domain.files.UserFile

interface GetFilesUseCase {
    suspend operator fun invoke() : List<UserFile>
}