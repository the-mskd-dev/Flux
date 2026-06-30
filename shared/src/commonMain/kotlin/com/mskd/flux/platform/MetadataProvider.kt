package com.mskd.flux.platform

import com.mskd.flux.model.domain.files.UserFile

interface MetadataProvider {
    suspend fun getDuration(file: UserFile) : Int
}