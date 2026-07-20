package com.mskd.flux.features.sources.presentation

import app.cash.turbine.test
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.DeleteSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        addSourceUseCase: AddSourceUseCase = mockk(relaxed = true),
        deleteSourceUseCase: DeleteSourceUseCase = mockk(relaxed = true),
        syncCatalogUseCase: SyncCatalogUseCase = mockk(relaxed = true)
    ): SourcesViewModel {

        val flowSourcesUseCase = mockk<FlowSourcesUseCase>()
        every { flowSourcesUseCase() } returns flowOf(folders)

        return SourcesViewModel(
            fromSetup = fromSetup,
            userDataStore = userDataStore,
            flowSourcesUseCase = flowSourcesUseCase,
            addSourceUseCase = addSourceUseCase,
            deleteSourceUseCase = deleteSourceUseCase,
            syncCatalogUseCase = syncCatalogUseCase
        )
    }

    // region uiState

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

    // endregion

    // region OpenFolderSelection / SaveFolder

    test("intent OpenFolderSelection emits event OpenFolderSelection") {
        val viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OpenFolderSelection)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.OpenFolderSelection
        }
    }

    test("SaveFolder calls addSourceUseCase with an AVAILABLE folder") {
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

    // endregion

    // region OnBackTap

    test("after SaveFolder, OnBackTap calls syncCatalogUseCase with onlyNew=true") {
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

    test("without SaveFolder, OnBackTap doesn't call syncCatalogUseCase") {
        val syncCatalogUseCase = mockk<SyncCatalogUseCase>(relaxed = true)
        val viewModel = createViewModel(syncCatalogUseCase = syncCatalogUseCase)

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OnBackTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.BackToPreviousScreen
        }

        coVerify(exactly = 0) { syncCatalogUseCase(onlyNew = any()) }
    }

    // endregion

    // region OnNextTap

    test("OnNextTap navigate to Catalog") {
        val viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(SourcesIntent.OnNextTap)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SourcesEvent.NavigateToCatalog
        }
    }

    // endregion

    // region Dialog

    test("ShowDeleteDialog updates dialog with ConfirmDelete for the given folder") {
        val folder = UserFolder(path = "path/x", isAvailable = true)
        val viewModel = createViewModel()

        viewModel.uiState.test {
            testDispatcher.scheduler.advanceUntilIdle()
            expectMostRecentItem()

            viewModel.handleIntent(SourcesIntent.ShowDeleteDialog(folder = folder))
            testDispatcher.scheduler.advanceUntilIdle()

            expectMostRecentItem().dialog shouldBe SourcesDialog.ConfirmDelete(folder = folder)
        }
    }

    test("CloseDialog resets dialog to null") {
        val folder = UserFolder(path = "path/x", isAvailable = true)
        val viewModel = createViewModel()

        viewModel.handleIntent(SourcesIntent.ShowDeleteDialog(folder = folder))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(SourcesIntent.CloseDialog)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.value.dialog shouldBe null
    }

    // endregion

    // region DeleteFolder

    test("DeleteFolder calls deleteSourceUseCase with the right folder then closes the dialog") {
        val folder = UserFolder(path = "path/x", isAvailable = true)
        val deleteSourceUseCase = mockk<DeleteSourceUseCase>(relaxed = true)
        val viewModel = createViewModel(deleteSourceUseCase = deleteSourceUseCase)

        viewModel.handleIntent(SourcesIntent.ShowDeleteDialog(folder = folder))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(SourcesIntent.DeleteFolder(folder = folder))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteSourceUseCase(folder = folder, deleteMedias = true) }
        viewModel.uiState.value.dialog shouldBe null
    }

    // endregion

})