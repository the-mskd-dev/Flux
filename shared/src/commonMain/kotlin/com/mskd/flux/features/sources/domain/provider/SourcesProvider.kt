package com.mskd.flux.features.sources.domain.provider

import com.mskd.flux.features.files.domain.datasource.FilesDataSource

interface SourcesProvider {
    suspend fun getSources(): List<FilesDataSource>
}