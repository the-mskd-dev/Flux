package com.mskd.flux.features.sources.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.sources.domain.model.UserFolder

@Immutable
data class SourcesUiState(
    val state: State<SourcesContent> = State.Loading,
    val showFeatureDialog: Boolean = false
)

@Immutable
data class SourcesContent(
    val fromSetup: Boolean = false,
    val folders: List<UserFolder> = emptyList(),
    val waitingDeleteFolder: UserFolder? = null,
    val systemFoldersEnabled: Boolean = true
)

sealed interface SourcesIntent {

    // Navigation
    data object OnBackTap: SourcesIntent
    data object OnNextTap: SourcesIntent

    // System
    data object OnSystemFoldersSwitch: SourcesIntent

    // Add
    data object OpenFolderSelection : SourcesIntent
    data class SaveFolder(val path: String) : SourcesIntent

    // Delete
    data class Delete(val folder: UserFolder) : SourcesIntent
    data object UndoDelete: SourcesIntent
    data object FinalizeDelete: SourcesIntent

    // Dialog
    data object CloseDialog: SourcesIntent

    // Permissions
    data object OnPermissionGranted: SourcesIntent
}

sealed interface SourcesEvent {

    // Navigation
    data object BackToPreviousScreen : SourcesEvent
    data object NavigateToCatalog: SourcesEvent
    data object NavigateToToken: SourcesEvent

    // Add
    data object OpenFolderSelection: SourcesEvent

    // Permissions
    data object ShowPermissionDialog: SourcesEvent
}
