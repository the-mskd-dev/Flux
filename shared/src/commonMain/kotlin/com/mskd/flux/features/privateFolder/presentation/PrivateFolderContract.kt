package com.mskd.flux.features.privateFolder.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.core.State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class PrivateFolderUiState(
    val screen: State<Unit> = State.Loading,
    val locked: Boolean = true,
    val artworks: ImmutableList<Artwork> = persistentListOf(),
    val pinInput: String = "",
    val pinError: Boolean = false
)

sealed interface PrivateFolderIntent {
    data object OnBackTap: PrivateFolderIntent
    data class SubmitPin(val pin: String): PrivateFolderIntent
    data class OnArtworkTap(val artwork: Artwork, val rgb: Int? = null): PrivateFolderIntent
    data class RemoveFromPrivateFolder(val artwork: Artwork): PrivateFolderIntent
}

sealed interface PrivateFolderEvent {
    data object BackToPreviousScreen: PrivateFolderEvent
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?): PrivateFolderEvent
    data class NavigateToShow(val artworkId: Long, val rgb: Int?): PrivateFolderEvent
}
