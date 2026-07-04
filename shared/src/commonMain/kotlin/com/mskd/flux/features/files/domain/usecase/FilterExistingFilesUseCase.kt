package com.mskd.flux.features.files.domain.usecase

import com.mskd.flux.core.domain.model.files.UserFile

interface FilterExistingFilesUseCase {
    suspend operator fun invoke(files: List<UserFile>) : List<UserFile>
}