package com.mskd.flux.features.sources.provider

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.setup.domain.model.SourceSelectionMode
import com.mskd.flux.features.sources.data.provider.SourcesProviderImpl
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class SourcesProviderImplTest : FunSpec({

    fluxExtensions()

    lateinit var settingsDataStore: SettingsDataStore
    lateinit var mediaStoreSource: FilesDataSource
    lateinit var safSource: FilesDataSource
    lateinit var settingsState: MutableStateFlow<SettingsDataStore.State>

    lateinit var provider: SourcesProviderImpl

    beforeTest {

        mediaStoreSource = mockk(relaxed = true)
        safSource = mockk(relaxed = true)

        settingsState = MutableStateFlow(SettingsDataStore.State(sourceSelectionMode = SourceSelectionMode.DEFAULT))
        settingsDataStore = mockk(relaxed = true)
        every { settingsDataStore.flow } returns settingsState

        provider = SourcesProviderImpl(
            settingsDataStore = settingsDataStore,
            mediaStoreSource = mediaStoreSource,
            safSource = safSource
        )

    }

    test("getSources - mode DEFAULT - returns MediaStore and SAF sources") {

        // Given
        settingsState.value = SettingsDataStore.State(sourceSelectionMode = SourceSelectionMode.DEFAULT)

        // When
        val sources = provider.getSources()

        // Then
        sources shouldContainExactly listOf(mediaStoreSource, safSource)

    }

    test("getSources - mode CUSTOM - returns only SAF source") {

        // Given
        settingsState.value = SettingsDataStore.State(sourceSelectionMode = SourceSelectionMode.CUSTOM)

        // When
        val sources = provider.getSources()

        // Then
        sources shouldContainExactly listOf(safSource)

    }

    test("getSources - mode changes between calls - reflects the new mode") {

        // Given
        settingsState.value = SettingsDataStore.State(sourceSelectionMode = SourceSelectionMode.DEFAULT)
        val firstCall = provider.getSources()

        // When
        settingsState.value = SettingsDataStore.State(sourceSelectionMode = SourceSelectionMode.CUSTOM)
        val secondCall = provider.getSources()

        // Then
        firstCall shouldContainExactly listOf(mediaStoreSource, safSource)
        secondCall shouldContainExactly listOf(safSource)

    }

})