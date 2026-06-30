package com.mskd.flux.platform

import com.mskd.flux.model.domain.UserFile

interface MetadataProvider {
    suspend fun getDuration(file: UserFile) : Int
}