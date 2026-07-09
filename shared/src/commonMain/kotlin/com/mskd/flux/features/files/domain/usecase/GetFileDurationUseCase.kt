package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.model.files.UserFile

interface GetFileDurationUseCase {

    companion object {
        const val TAG = "GetFileDuration"
    }

    suspend operator fun invoke(file: UserFile): Int
}