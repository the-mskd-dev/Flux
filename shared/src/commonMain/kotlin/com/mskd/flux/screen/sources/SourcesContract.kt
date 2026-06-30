package com.mskd.flux.screen.sources

import androidx.compose.runtime.Immutable
import com.mskd.flux.model.core.presentation.State
import com.mskd.flux.model.domain.files.UserFolder

@Immutable
data class SourcesUiState(
    val state: State<SourcesContent> = State.Loading
)

@Immutable
data class SourcesContent(
    val folders: List<UserFolder> = emptyList()
)

sealed class SourcesIntent {
    data object OnBackTap : SourcesIntent()
    data object AddFolders : SourcesIntent()
    data class DeleteFolder(val folder: UserFolder) : SourcesIntent()
}

sealed class SourcesEvent {
    data object BackToPreviousScreen : SourcesEvent()
    data object OpenFolderSelection: SourcesEvent()
}
