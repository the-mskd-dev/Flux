package com.mskd.flux.screens.unknown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.screens.artwork.ArtworkEvent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnknownViewModel @Inject constructor(private val repository: ArtworkRepository) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<UnknownEvent>()
    val event = _event.asSharedFlow().distinctUntilChanged()

    val uiState: StateFlow<UnknownUiState> = repository.flow
        .map { artwork ->
            UnknownUiState(
                screen = ScreenState.CONTENT,
                medias = artwork.episodes.sortedWith(
                    compareBy<Episode> { it.title }.thenBy { it.season }.thenBy { it.number }
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UnknownUiState()
        )

    //endregion

    //region Init

    init {
        repository.searchArtwork(artworkId = Artwork.UNKNOWN_ID)
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: UnknownIntent) = viewModelScope.launch {
        when (intent) {
            is UnknownIntent.PlayMedia -> _event.emit(UnknownEvent.PlayMedia(mediaId = intent.media.mediaId))
            UnknownIntent.OnBackTap -> _event.emit(UnknownEvent.BackToPreviousScreen)
        }
    }

    //endregion

}