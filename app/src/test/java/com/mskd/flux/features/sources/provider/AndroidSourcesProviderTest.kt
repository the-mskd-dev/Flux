package com.mskd.flux.features.sources.provider

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.sources.data.provider.AndroidSourcesProvider
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.fake.FakeSourcesRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class AndroidSourcesProviderImplTest : FunSpec({

    fluxExtensions()

    val availableFolder = UserFolder(path = "content://tree/movies", source = FileSource.SAF, isAvailable = true)
    val unavailableFolder = UserFolder(path = "content://tree/sdcard", source = FileSource.SAF, isAvailable = false)

    lateinit var settingsState: MutableStateFlow<SettingsDataStore.State>
    lateinit var mediaStoreSource: FilesDataSource
    lateinit var provider: AndroidSourcesProvider

    beforeTest {

        mediaStoreSource = FakeFilesDataSource(availableFiles = emptyList())

        settingsState = MutableStateFlow(SettingsDataStore.State(systemFoldersEnabled = true))
        val settingsDataStore = mockk<SettingsDataStore>(relaxed = true)
        every { settingsDataStore.flow } returns settingsState

        provider = AndroidSourcesProvider(
            settingsDataStore = settingsDataStore,
            sourcesRepository = FakeSourcesRepository(initFolders = listOf(availableFolder, unavailableFolder)),
            mediaStoreSource = mediaStoreSource,
            safSourceFor = { folder -> FakeFilesDataSource(availableFiles = emptyList(), available = folder.isAvailable, root = folder.path) }
        )

    }

    test("getSources - system folders enabled - returns the device storage and one source per custom folder") {

        // When
        val sources = provider.getSources()

        // Then
        sources.map { it.isAvailable() } shouldContainExactly listOf(true, true, false)
        sources.first() shouldBe mediaStoreSource

    }

    test("getSources - system folders disabled - returns only the custom folders") {

        // Given
        settingsState.value = SettingsDataStore.State(systemFoldersEnabled = false)

        // When
        val sources = provider.getSources()

        // Then
        sources.map { it.isAvailable() } shouldContainExactly listOf(true, false)

    }

    test("getSources - mode changes between calls - reflects the new mode") {

        // Given
        val firstCall = provider.getSources()

        // When
        settingsState.value = SettingsDataStore.State(systemFoldersEnabled = false)
        val secondCall = provider.getSources()

        // Then
        firstCall.size shouldBe 3
        secondCall.size shouldBe 2

    }

})
