package com.mskd.flux.features.show.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.core.State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class ShowUiState(
    val state: State<ShowContent> = State.Loading
)

@Immutable
data class ShowContent(
    val fullShow: FullArtwork.FullShow,
    val credits: ImmutableList<Person> = persistentListOf(),
    val dialog: ShowDialog? = null,
)

sealed class ShowDialog {
    data class SeasonPreview(val season: Season) : ShowDialog()
    object ResetProgress : ShowDialog()
}

sealed class ShowIntent {
    // Navigation
    object OnBackTap: ShowIntent()
    data class OnSeasonClick(val season: Int, val rgb: Int?) : ShowIntent()
    data class OnPersonClick(val person: Person) : ShowIntent()

    // Dialogs
    data object CloseDialog : ShowIntent()
    data class ShowSeasonPreview(val season: Season) : ShowIntent()
    data object ShowResetProgressDialog : ShowIntent()

    // Other
    data object OpenShowInfo: ShowIntent()
    data object ResetProgress: ShowIntent()
}

sealed class ShowEvent {
    object BackToPreviousScreen : ShowEvent()
    data class NavigateToSeason(val artworkId: Long, val season: Int, val rgb: Int?) : ShowEvent()
    data class NavigateToPerson(val person: Person) : ShowEvent()
}