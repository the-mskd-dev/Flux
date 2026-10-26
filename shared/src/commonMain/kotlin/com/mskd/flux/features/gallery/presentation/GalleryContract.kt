package com.mskd.flux.features.gallery.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.gallery.domain.model.GalleryImage

@Immutable
data class GalleryUiState(
    val title: String = "",
    val state: State<GalleryContent> = State.Loading
)

@Immutable
data class GalleryContent(
    val images: List<GalleryImage>
)

sealed interface GalleryIntent {
    data object OnBackTap : GalleryIntent
}

sealed interface GalleryEvent {
    data object BackToPreviousScreen : GalleryEvent
}
