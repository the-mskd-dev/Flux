package com.mskd.flux.features.sources.data.datasource

import com.mskd.flux.core.domain.model.files.UserFile

interface SourcesDataSource {
    suspend fun getFiles(): List<UserFile>
}