package com.mskd.flux.features.sources.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.DeleteSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SourcesViewModel(
    private val fromSetup: Boolean,
    private val userDataStore: UserDataStore,
    flowSourcesUseCase: FlowSourcesUseCase,
    private val addSourceUseCase: AddSourceUseCase,
    private val deleteSourceUseCase: DeleteSourceUseCase,
    private val syncCatalogUseCase: SyncCatalogUseCase,
) : ViewModel() {

    //region State

    private val _event = Channel<SourcesEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _dialogState = MutableStateFlow<SourcesDialog?>(null)

    val uiState = combine(
        flowSourcesUseCase(),
        _dialogState
    ) { folders, dialog ->

        SourcesUiState(
            state = State.Content(
                content = SourcesContent(
                    fromSetup = fromSetup,
                    folders = folders
                )
            ),
            dialog = dialog
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SourcesUiState()
    )

    private val intentChannel = Channel<SourcesIntent>(Channel.UNLIMITED)

    //endregion

    //region Variables

    private var needSync = false

    //endregion

    //region Init

    init {

        viewModelScope.launch {
            intentChannel.receiveAsFlow().collect { intent ->
                processIntent(intent)
            }
        }

        // TODO: Delete in October 2026
        // Show new feature dialog
        viewModelScope.launch {

            if (userDataStore.getVersionCode() in 1..27) {
                _dialogState.update { SourcesDialog.NewFeatureInformation }
            }

        }

    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: SourcesIntent) {
        intentChannel.trySend(intent)
    }

    //endregion

    //region Private Methods

    private fun processIntent(intent: SourcesIntent) = viewModelScope.launch {
        when (intent) {
            SourcesIntent.OnBackTap -> onBackTap()
            SourcesIntent.OnNextTap -> onNextTap()

            // Save
            SourcesIntent.OpenFolderSelection -> _event.send(SourcesEvent.OpenFolderSelection)
            is SourcesIntent.SaveFolder -> saveFolder(path = intent.path)

            // Delete
            is SourcesIntent.DeleteFolder -> deleteFolder(folder = intent.folder)

            // Dialog
            is SourcesIntent.ShowDeleteDialog -> showDeleteDialog(folder = intent.folder)
            SourcesIntent.CloseDialog -> closeDialog()
        }
    }

    private suspend fun onBackTap() {
        if (needSync) syncCatalogUseCase(onlyNew = true)
        _event.send(SourcesEvent.BackToPreviousScreen)
    }

    private suspend fun onNextTap() {
        _event.send(SourcesEvent.NavigateToCatalog)
    }

    private suspend fun saveFolder(path: String) {

        val folder = UserFolder(
            path = path,
            source = FileSource.SAF,
            isAvailable = true
        )

        addSourceUseCase(folder = folder)

        needSync = true

    }

    private fun showDeleteDialog(folder: UserFolder) {
        _dialogState.update { SourcesDialog.ConfirmDelete(folder = folder) }
    }

    private fun closeDialog() {
        _dialogState.update { null }
    }

    private suspend fun deleteFolder(folder: UserFolder) {
        deleteSourceUseCase(folder = folder, deleteMedias = true)
        closeDialog()
    }

    //endregion

}