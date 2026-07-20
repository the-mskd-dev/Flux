package com.mskd.flux.features.sources.domain.validator

interface UserFolderValidator {
    suspend fun isFolderAvailable(path: String): Boolean
}