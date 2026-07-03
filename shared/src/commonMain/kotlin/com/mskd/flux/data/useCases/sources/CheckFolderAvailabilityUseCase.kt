package com.mskd.flux.data.useCases.sources

import com.mskd.flux.model.domain.files.UserFolder

interface CheckFolderAvailabilityUseCase {
    suspend operator fun invoke(path: String): UserFolder.Status
}