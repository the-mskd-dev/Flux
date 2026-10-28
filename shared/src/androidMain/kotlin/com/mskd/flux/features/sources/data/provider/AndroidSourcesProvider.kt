package com.mskd.flux.features.sources.data.provider

import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import kotlinx.coroutines.flow.first

/**
 * Provides the device storage, when enabled, and one Source per custom folder.
 */
class AndroidSourcesProvider(
    private val settingsDataStore: SettingsDataStore,
    private val sourcesRepository: SourcesRepository,
    private val mediaStoreSource: FilesDataSource,
    private val safSourceFor: (UserFolder) -> FilesDataSource,
) : SourcesProvider {

    override suspend fun getSources(): List<FilesDataSource> {
        val customSources = sourcesRepository.getFolders().map(safSourceFor)
        return when (settingsDataStore.flow.first().systemFoldersEnabled) {
            true -> listOf(mediaStoreSource) + customSources
            false -> customSources
        }
    }

}