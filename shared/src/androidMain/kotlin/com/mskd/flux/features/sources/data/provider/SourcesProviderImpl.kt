package com.mskd.flux.features.sources.data.provider

import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.setup.domain.model.SourceSelectionMode
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import kotlinx.coroutines.flow.first

class SourcesProviderImpl(
    private val settingsDataStore: SettingsDataStore,
    private val mediaStoreSource: FilesDataSource,
    private val safSource: FilesDataSource,
) : SourcesProvider {

    override suspend fun getSources(): List<FilesDataSource> {
        return when (settingsDataStore.flow.first().sourceSelectionMode) {
            SourceSelectionMode.DEFAULT -> listOf(mediaStoreSource, safSource)
            SourceSelectionMode.CUSTOM -> listOf(safSource)
        }
    }

}