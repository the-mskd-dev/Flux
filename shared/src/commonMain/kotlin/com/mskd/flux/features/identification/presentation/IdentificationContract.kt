package com.mskd.flux.features.identification.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.core.State
import kotlinx.collections.immutable.ImmutableList

/** A TMDB result of the dialog. The current Artwork is marked and cannot be chosen. */
@Immutable
data class IdentificationCandidate(
    val artwork: Artwork,
    val year: Int?,
    val isCurrent: Boolean,
)

@Immutable
data class IdentificationUiState(
    val query: String = "",
    val results: State<ImmutableList<IdentificationCandidate>> = State.Loading,
    val isIdentifying: Boolean = false,
    val identificationFailed: Boolean = false,
)

sealed interface IdentificationIntent {
    data class UpdateQuery(val query: String) : IdentificationIntent
    /** Searches the query again: submitted from the keyboard or retried after an error. */
    data object Search : IdentificationIntent
    data class Select(val candidate: IdentificationCandidate) : IdentificationIntent
}

sealed interface IdentificationEvent {
    /** The Médias now belong to [artworkId]. */
    data class Identified(val artworkId: Long) : IdentificationEvent
}
