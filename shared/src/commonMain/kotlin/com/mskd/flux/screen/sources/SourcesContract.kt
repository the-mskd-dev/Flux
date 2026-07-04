package com.mskd.flux.screen.sources

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.domain.model.core.State
import com.mskd.flux.features.sources.domain.model.UserFolder

@Immutable
data class SourcesUiState(
    val state: State<SourcesContent> = State.Loading,
    val dialog: SourcesDialog? = null
)

@Immutable
data class SourcesContent(
    val folders: List<UserFolder> = emptyList()
)

sealed class SourcesDialog {
    data class ConfirmDelete(val folder: UserFolder) : SourcesDialog()
}

sealed class SourcesIntent {
    data object OnBackTap : SourcesIntent()
    data object OpenFolderSelection : SourcesIntent()
    data class SaveFolder(val path: String) : SourcesIntent()
    data class ShowDeleteDialog(val folder: UserFolder) : SourcesIntent()
    data object CloseDeleteDialog : SourcesIntent()
    data class DeleteFolder(val folder: UserFolder) : SourcesIntent()
}

sealed class SourcesEvent {
    data object BackToPreviousScreen : SourcesEvent()
    data object OpenFolderSelection: SourcesEvent()
}
