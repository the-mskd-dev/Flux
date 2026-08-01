package com.mskd.flux.features.sources.data.provider

import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import kotlinx.coroutines.flow.first

class AndroidSourcesProvider(
    private val settingsDataStore: SettingsDataStore,
    private val mediaStoreSource: FilesDataSource,
    private val safSource: FilesDataSource,
) : SourcesProvider {

    override suspend fun getSources(): List<FilesDataSource> {
        return when (settingsDataStore.flow.first().systemFoldersEnabled) {
            true -> listOf(mediaStoreSource, safSource)
            false -> listOf(safSource)
        }
    }

}