package com.mskd.flux.data.repository.sources

import com.mskd.flux.model.domain.files.UserFolder

interface CheckFolderAvailabilityDataSource {
    suspend fun isFolderAvailable(path: String): UserFolder.Status
}