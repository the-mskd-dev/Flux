package com.mskd.flux.features.movie.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.identification.presentation.IdentificationAvailability
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class MovieContent(
    val fullMovie: FullArtwork.FullMovie,
    val overview: ArtworkOverviewContent.MovieOverview,
    val people: ImmutableList<Person> = persistentListOf(),
    val identification: IdentificationAvailability = IdentificationAvailability.HIDDEN,
    val dialog: MoviePageDialog? = null,
)

@Immutable
data class MovieUiState(
    val state: State<MovieContent> = State.Loading
)

sealed interface MoviePageDialog {
    data object ResetProgressConfirmation : MoviePageDialog
    data object Identification : MoviePageDialog
}

sealed interface MovieIntent {
    // Navigation
    data object OnBackTap : MovieIntent
    data class PlayMedia(val media: Media, val forceInternal: Boolean = false) : MovieIntent
    data object OpenMovieInfo : MovieIntent
    data class OpenFileExplorer(val media: Media) : MovieIntent
    data class OnPersonClick(val person: Person) : MovieIntent
    data object OnArtworkImageClick : MovieIntent

    // Dialogs
    data object CloseDialog : MovieIntent
    data object ShowResetProgressDialog : MovieIntent
    data object ShowIdentificationDialog : MovieIntent

    // Manual identification
    data class OnIdentified(val artworkId: Long, val type: ContentType) : MovieIntent

    // Status
    data object ResetProgress : MovieIntent
    data class ChangeWatchStatus(val media: Media) : MovieIntent

    // Other
    data class OnExternalPlayerResult(val progress: Long) : MovieIntent
}

sealed interface MovieEvent {
    data object BackToPreviousScreen : MovieEvent
    data class PlayMedia(val media: Media, val externalPlayer: Boolean) : MovieEvent
    data class NavigateToPerson(val person: Person) : MovieEvent
    data class NavigateToGallery(val artworkId: Long, val contentType: ContentType) : MovieEvent
    /**
     * The Media now belong to [artworkId], of the given [type]: the navigation replaces the page by the one matching [type].
     * Back must not lead to the previous Artwork, which may no longer exist.
     */
    data class Identified(val artworkId: Long, val type: ContentType) : MovieEvent
}
