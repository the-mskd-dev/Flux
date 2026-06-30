package com.mskd.flux.screen.sources

import com.mskd.flux.model.domain.files.UserFolder

data class SourcesUiState(
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
