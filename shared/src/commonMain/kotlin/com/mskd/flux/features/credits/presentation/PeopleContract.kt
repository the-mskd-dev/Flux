package com.mskd.flux.features.credits.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.credits.domain.model.PersonDetails

@Immutable
data class PeopleUiState(
    val state: State<PeopleContent> = State.Loading
)

@Immutable
data class PeopleContent(
    val details: PersonDetails
)

sealed interface PeopleIntent {
    // Navigation
    data object OnBackTap: PeopleIntent
    data class OnArtworkClick(val artwork: Artwork, val rgb: Int?) : PeopleIntent
}

sealed interface PeopleEvent {
    object BackToPreviousScreen : PeopleEvent
    data class NavigateToMovie(val artworkId: Long, val rgb: Int?) : PeopleEvent
    data class NavigateToShow(val artworkId: Long, val rgb: Int?) : PeopleEvent
}