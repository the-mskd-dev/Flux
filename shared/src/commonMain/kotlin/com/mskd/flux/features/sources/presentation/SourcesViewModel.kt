package com.mskd.flux.features.sources.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val settingsDataStore: SettingsDataStore,
    private val tokenDataStore: TokenDataStore,
    flowSourcesUseCase: FlowSourcesUseCase,
    private val addSourceUseCase: AddSourceUseCase,
    private val deleteSourceUseCase: DeleteSourceUseCase,
    private val syncCatalogUseCase: SyncCatalogUseCase,
) : ViewModel() {

    //region State

    private val _event = Channel<SourcesEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _deleteState = MutableStateFlow<UserFolder?>(null)
    private val _showDialogState = MutableStateFlow(false)

    val uiState = combine(
        flowSourcesUseCase(),
        _deleteState,
        _showDialogState,
        settingsDataStore.flow
    ) { folders, deleteState, showDialogState, settings ->

        SourcesUiState(
            state = State.Content(
                content = SourcesContent(
                    fromSetup = fromSetup,
                    folders = folders,
                    waitingDeleteFolder = deleteState,
                    systemFoldersEnabled = settings.systemFoldersEnabled
                )
            ),
            showFeatureDialog = showDialogState
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
                _showDialogState.update { true }
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

            // System
            SourcesIntent.OnSystemFoldersSwitch -> onSystemFoldersSwitch()

            // Save
            SourcesIntent.OpenFolderSelection -> _event.send(SourcesEvent.OpenFolderSelection)
            is SourcesIntent.SaveFolder -> saveFolder(path = intent.path)

            // Delete
            is SourcesIntent.Delete -> delete(folder = intent.folder)
            SourcesIntent.UndoDelete -> undoDelete()
            SourcesIntent.FinalizeDelete -> finalizeDelete()

            // Dialog
            SourcesIntent.CloseDialog -> closeDialog()

            // Permissions
            SourcesIntent.OnPermissionGranted -> onPermissionGranted()
        }
    }

    private suspend fun onBackTap() {
        finalizeDelete()

        if (needSync) syncCatalogUseCase(onlyNew = true)
        _event.send(SourcesEvent.BackToPreviousScreen)
    }

    private suspend fun onNextTap() {
        finalizeDelete()

        if (tokenDataStore.tokenRequested)
            _event.send(SourcesEvent.NavigateToToken)
        else
            _event.send(SourcesEvent.NavigateToCatalog)
    }

    private suspend fun saveFolder(path: String) {
        finalizeDelete()

        val folder = UserFolder(
            path = path,
            source = FileSource.SAF,
            isAvailable = true
        )

        addSourceUseCase(folder = folder)

        needSync = true

    }

    private suspend fun delete(folder: UserFolder) {
        finalizeDelete()

        _deleteState.update { folder }
    }

    private fun undoDelete() {
        _deleteState.update { null }
    }

    private suspend fun finalizeDelete() {
        val folder = _deleteState.value ?: return
        deleteSourceUseCase(folder = folder, deleteMedias = true)
        _deleteState.update { null }
    }

    private fun closeDialog() {
        _showDialogState.update { false }
    }

    private suspend fun onSystemFoldersSwitch() {
        val isEnabled = (uiState.value.state as? State.Content)?.content?.systemFoldersEnabled ?: return

        if (!isEnabled) {
            _event.send(SourcesEvent.ShowPermissionDialog)
        } else {
            settingsDataStore.setSystemFolders(enabled = false)
            needSync = true
        }

    }

    private suspend fun onPermissionGranted() {
        settingsDataStore.setSystemFolders(enabled = true)
        needSync = true
    }

    //endregion

}