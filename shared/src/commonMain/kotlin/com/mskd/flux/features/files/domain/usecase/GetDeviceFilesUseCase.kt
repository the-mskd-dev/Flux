package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile

interface GetDeviceFilesUseCase {
    suspend operator fun invoke() : List<UserFile>
}