package com.mskd.flux.screen.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.useCases.sources.AddSourceUseCase
import com.mskd.flux.data.useCases.sources.DeleteSourceUseCase
import com.mskd.flux.data.useCases.sources.FlowSourcesUseCase
import com.mskd.flux.model.core.presentation.State
import com.mskd.flux.model.domain.files.UserFolder
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SourcesViewModel(
    flowSourcesUseCase: FlowSourcesUseCase,
    val addSourceUC: AddSourceUseCase,
    val deleteSourceUseCase: DeleteSourceUseCase
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
                content = SourcesContent(folders = folders)
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

    //region Init

    init {

        viewModelScope.launch {
            intentChannel.receiveAsFlow().collect { intent ->
                processIntent(intent)
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
            SourcesIntent.OnBackTap -> _event.send(SourcesEvent.BackToPreviousScreen)

            // Save
            SourcesIntent.OpenFolderSelection -> _event.send(SourcesEvent.OpenFolderSelection)
            is SourcesIntent.SaveFolder -> saveFolder(path = intent.path)

            // Delete
            is SourcesIntent.ShowDeleteDialog -> showDeleteDialog(folder = intent.folder)
            SourcesIntent.CloseDeleteDialog -> closeDeleteDialog()
            is SourcesIntent.DeleteFolder -> deleteFolder(folder = intent.folder)
        }
    }

    private suspend fun saveFolder(path: String) {

        val folder = UserFolder(
            path = path,
            status = UserFolder.Status.AVAILABLE
        )

        val result = addSourceUC(folder = folder)

        if (result) {
            Trace.debug(message = "Folder saved")
        } else {
            Trace.debug(message = "Folder not saved")
        }
    }

    private fun showDeleteDialog(folder: UserFolder) {
        _dialogState.update { SourcesDialog.ConfirmDelete(folder = folder) }
    }

    private fun closeDeleteDialog() {
        _dialogState.update { null }
    }

    private suspend fun deleteFolder(folder: UserFolder) {
        deleteSourceUseCase(folder = folder)
        closeDeleteDialog()
    }

    //endregion

}