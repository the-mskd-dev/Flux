package com.mskd.flux.features.sources.domain.validator

import com.mskd.flux.features.sources.domain.model.UserFolder

interface UserFolderValidator {
    suspend fun isFolderAvailable(path: String): Boolean
}