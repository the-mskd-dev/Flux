package com.mskd.flux.features.sources.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.sources.domain.model.UserFolder

@Immutable
data class SourcesUiState(
    val state: State<SourcesContent> = State.Loading,
    val dialog: SourcesDialog? = null
)

@Immutable
data class SourcesContent(
    val fromSetup: Boolean = false,
    val folders: List<UserFolder> = emptyList()
)

sealed interface SourcesDialog {
    data class ConfirmDelete(val folder: UserFolder) : SourcesDialog
    data object NewFeatureInformation: SourcesDialog
}

sealed interface SourcesIntent {

    // Navigation
    data object OnBackTap: SourcesIntent
    data object OnNextTap: SourcesIntent

    // Add
    data object OpenFolderSelection : SourcesIntent
    data class SaveFolder(val path: String) : SourcesIntent

    // Delete
    data class DeleteFolder(val folder: UserFolder) : SourcesIntent

    // Dialog
    data class ShowDeleteDialog(val folder: UserFolder) : SourcesIntent
    data object CloseDialog : SourcesIntent
}

sealed interface SourcesEvent {

    // Navigation
    data object BackToPreviousScreen : SourcesEvent
    data object NavigateToCatalog: SourcesEvent

    // Add
    data object OpenFolderSelection: SourcesEvent
}
