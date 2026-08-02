package com.mskd.flux.features.sources.presentation

import app.cash.turbine.test
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.DeleteSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SourcesViewModelTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeTest { Dispatchers.setMain(testDispatcher) }
    afterTest { Dispatchers.resetMain() }

    fun createViewModel(
        fromSetup: Boolean = false,
        folders: List<UserFolder> = emptyList(),
        userDataStore: UserDataStore = mockk(relaxed = true),
        settingsDataStore: SettingsDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(SettingsDataStore.State())
        },
        tokenDataStore: TokenDataStore = mockk(relaxed = true),
        addSourceUseCase: AddSourceUseCase = mockk(relaxed = true),
        deleteSourceUseCase: DeleteSourceUseCase = mockk(relaxed = true),
        syncCatalogUseCase: SyncCatalogUseCase = mockk(relaxed = true),
    ): SourcesViewModel {

        val flowSourcesUseCase = mockk<FlowSourcesUseCase>()
        every { flowSourcesUseCase() } returns flowOf(folders)

        return SourcesViewModel(
            fromSetup = fromSetup,
            userDataStore = userDataStore,
            settingsDataStore = settingsDataStore,
            tokenDataStore = tokenDataStore,
            flowSourcesUseCase = flowSourcesUseCase,
            addSourceUseCase = addSourceUseCase,
            deleteSourceUseCase = deleteSourceUseCase,
            syncCatalogUseCase = syncCatalogUseCase
        )
    }

    //region uiState

    test("uiState returns folders from flowSourcesUseCase") {
        val folders = listOf(
            UserFolder(path = "path/1", isAvailable = true),
            UserFolder(path = "path/2", isAvailable = true)
        )
        val viewModel = createViewModel(folders = folders)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()
            (state.state as State.Content).content.folders shouldBe folders
        }
    }

    test("uiState returns fromSetup in content") {
        checkAll(Arb.boolean()) { fromSetup ->
            val viewModel = createViewModel(fromSetup = fromSetup)

            viewModel.uiState.test {
                testDispatcher.scheduler.advanceUntilIdle()
                val state = expectMostRecentItem()
                (state.state as State.Content).content.fromSetup shouldBe fromSetup
            }
        }
    }

    //endregion

    //region Navigation

    test("OnBackTap - after SaveFolder, calls syncCatalogUseCase with onlyNew=true") {
        val syncCatalogUseCase = mockk<SyncCatalogUseCase>(relaxed = true)
        val viewModel = createViewModel(syncCatalogUseCase = syncCatalogUseCase)

        viewModel.handleIntent(SourcesIntent.SaveFolder(path = "content://some/path"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OnBackTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.BackToPreviousScreen
        }

        coVerify { syncCatalogUseCase(onlyNew = true) }
    }

    test("OnBackTap - without SaveFolder, doesn't call syncCatalogUseCase") {
        val syncCatalogUseCase = mockk<SyncCatalogUseCase>(relaxed = true)
        val viewModel = createViewModel(syncCatalogUseCase = syncCatalogUseCase)

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OnBackTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.BackToPreviousScreen
        }

        coVerify(exactly = 0) { syncCatalogUseCase(onlyNew = any()) }
    }

    test("OnNextTap - navigate to Token if needed") {
        val tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            coEvery { tokenRequested } returns true
        }
        val viewModel = createViewModel(tokenDataStore = tokenDataStore)

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OnNextTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.NavigateToToken
        }
    }

    test("OnNextTap - navigate to Catalog if no token is needed") {
        val tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            coEvery { tokenRequested } returns false
        }
        val viewModel = createViewModel(tokenDataStore = tokenDataStore)

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OnNextTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.NavigateToCatalog
        }
    }

    // endregion

    //region System

    test("OnSystemFoldersSwitch - if value is set to true, show permissions") {
        // Given
        val settingsDataStore = mockk<SettingsDataStore>(relaxed = true) {
            coEvery { flow } returns MutableStateFlow(SettingsDataStore.State(systemFoldersEnabled = false))
        }
        val viewModel = createViewModel(settingsDataStore = settingsDataStore)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.event.test {
                // When
                viewModel.handleIntent(SourcesIntent.OnSystemFoldersSwitch)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                awaitItem() shouldBe SourcesEvent.ShowPermissionDialog
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("OnSystemFoldersSwitch - if value is set to false, disable system folders") {
        val settingsDataStore = mockk<SettingsDataStore>(relaxed = true) {
            coEvery { flow } returns MutableStateFlow(SettingsDataStore.State(systemFoldersEnabled = true))
        }
        val viewModel = createViewModel(settingsDataStore = settingsDataStore)

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()

            // When
            viewModel.handleIntent(SourcesIntent.OnSystemFoldersSwitch)
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            coVerify { settingsDataStore.setSystemFolders(enabled = false) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    //endregion

    //region Add

    test("OpenFolderSelection - emits event OpenFolderSelection") {
        val viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OpenFolderSelection)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.OpenFolderSelection
        }
    }

    test("SaveFolder - calls addSourceUseCase with an AVAILABLE folder") {
        val addSourceUseCase = mockk<AddSourceUseCase>(relaxed = true)
        val viewModel = createViewModel(addSourceUseCase = addSourceUseCase)

        viewModel.handleIntent(SourcesIntent.SaveFolder(path = "content://some/path"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            addSourceUseCase(
                folder = UserFolder(path = "content://some/path", isAvailable = true, source = FileSource.SAF)
            )
        }
    }

    //endregion

    //region Delete

    test("Delete - put the folder in waitingDeleteFolder") {
        // Given
        val folder = UserFolder(path = "path/x", isAvailable = true)
        val viewModel = createViewModel()

        // When
        viewModel.handleIntent(SourcesIntent.Delete(folder = folder))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val content = (expectMostRecentItem().state as? State.Content)?.content
            content?.waitingDeleteFolder shouldBe folder
        }
    }

    test("Delete - finalize previous deletion, then put the folder in waitingDeleteFolder") {
        // Given
        val folder = UserFolder(path = "path/x", isAvailable = true)
        val folder2 = UserFolder(path = "path/y", isAvailable = true)
        val deleteSourceUseCase = mockk<DeleteSourceUseCase>(relaxed = true)
        val viewModel = createViewModel(deleteSourceUseCase = deleteSourceUseCase)

        viewModel.handleIntent(SourcesIntent.Delete(folder = folder))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.handleIntent(SourcesIntent.Delete(folder = folder2))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { deleteSourceUseCase(folder = folder, deleteMedias = true) }

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val content = (expectMostRecentItem().state as? State.Content)?.content
            content?.waitingDeleteFolder shouldBe folder2
        }
    }

    test("UndoDelete - remove folder from waitingDeleteFolder") {
        // Given
        val folder = UserFolder(path = "path/x", isAvailable = true)
        val viewModel = createViewModel()
        viewModel.handleIntent(SourcesIntent.Delete(folder = folder))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.handleIntent(SourcesIntent.UndoDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val content = (expectMostRecentItem().state as? State.Content)?.content
            content?.waitingDeleteFolder shouldBe null
        }
    }

    test("FinalizeDelete - calls deleteSourceUseCase") {
        // Given
        val folder = UserFolder(path = "path/x", isAvailable = true)
        val deleteSourceUseCase = mockk<DeleteSourceUseCase>(relaxed = true)
        val viewModel = createViewModel(deleteSourceUseCase = deleteSourceUseCase)
        viewModel.handleIntent(SourcesIntent.Delete(folder = folder))
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.handleIntent(SourcesIntent.FinalizeDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { deleteSourceUseCase(folder = folder, deleteMedias = true) }

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val content = (expectMostRecentItem().state as? State.Content)?.content
            content?.waitingDeleteFolder shouldBe null
        }
    }

    //endregion

    //region Permissions

    test("OnPermissionGranted - set system folders to true") {
        // Given
        val settingsDataStore = mockk<SettingsDataStore>(relaxed = true)
        val viewModel = createViewModel(settingsDataStore = settingsDataStore)

        // When
        viewModel.handleIntent(SourcesIntent.OnPermissionGranted)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsDataStore.setSystemFolders(enabled = true) }
    }

    //endregion

})