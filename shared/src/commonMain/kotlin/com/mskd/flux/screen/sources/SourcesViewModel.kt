package com.mskd.flux.screen.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.model.core.presentation.State
import com.mskd.flux.model.domain.files.UserFolder
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class SourcesViewModel : ViewModel() {

    //region State

    private val _uiState = MutableStateFlow(SourcesUiState())
    val uiState = _uiState.asStateFlow()

    private val intentChannel = Channel<SourcesIntent>(Channel.UNLIMITED)

    private val _event = Channel<SourcesEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    //endregion

    //region Init

    init {

        viewModelScope.launch {
            intentChannel.receiveAsFlow().collect { intent ->
                processIntent(intent)
            }
        }

        viewModelScope.launch {

            delay(3.seconds)
            _uiState.update { it.copy(state = State.Content(SourcesContent())) }

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
            SourcesIntent.AddFolders -> addFolders()
            is SourcesIntent.DeleteFolder -> deleteFolder(folder = intent.folder)
        }
    }

    private suspend fun addFolders() {
        _event.send(SourcesEvent.OpenFolderSelection)
    }

    private suspend fun deleteFolder(folder: UserFolder) {
        Trace.debug(message = "Delete folder : ${folder.path}")
    }

    //endregion

}