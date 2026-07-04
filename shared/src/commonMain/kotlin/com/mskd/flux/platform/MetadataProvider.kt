package com.mskd.flux.platform

import com.mskd.flux.core.domain.model.files.UserFile

interface MetadataProvider {
    suspend fun getDuration(file: UserFile) : Int
}