package com.mskd.flux.features.files.domain.datasource

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.model.ExternalSubtitle

interface FilesDataSource {
    suspend fun isAvailable() : Boolean
    fun contains(file: UserFile) : Boolean
    suspend fun getFiles() : List<UserFile>
    suspend fun getSubtitlesFor(file: UserFile) : List<ExternalSubtitle>
}