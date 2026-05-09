package com.mskd.flux.screens.unknown

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.progress.ProgressUC
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnknownViewModel @Inject constructor(
    private val artworkUC: ArtworkUC,
    private val settingsRepository: SettingsRepository,
    private val progressUC: ProgressUC
) : ViewModel() {

    //region Variables

    private var selectedMedia: Media? = null

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<UnknownEvent>()
    val event = _event.asSharedFlow()

    val uiState: StateFlow<UnknownUiState> = combine(
        artworkUC.flow,
        settingsRepository.flow
    ) { artworkContent, settings ->
        UnknownUiState(
            screen = ScreenState.CONTENT,
            medias = (artworkContent as? ArtworkUC.Content.SHOW)?.episodes?.sortedWith(
                compareBy<Episode> { it.title }.thenBy { it.season }.thenBy { it.number }
            ) ?: emptyList(),
            useExternalPlayer = settings.externalPlayer
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
        artworkUC.searchArtwork(artworkId = Artwork.UNKNOWN_ID)
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: UnknownIntent) = viewModelScope.launch {
        when (intent) {
            is UnknownIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            UnknownIntent.OnBackTap -> _event.emit(UnknownEvent.BackToPreviousScreen)
            UnknownIntent.OnInfoTap -> _event.emit(UnknownEvent.NavigateToHowToScreen)
            is UnknownIntent.OnExternalPlayerResult -> onExternalPlayerResult(progress = intent.progress)
        }
    }

    //endregion

    //region Private Methods

    private suspend fun playMedia(media: Media, forceInternal: Boolean) {

        selectedMedia = media

        val event = if (uiState.value.useExternalPlayer && !forceInternal)
            UnknownEvent.LaunchExternalPlayer(media = media)
        else
            UnknownEvent.PlayMedia(mediaId = media.mediaId)

        _event.emit(event)
    }

    private suspend fun onExternalPlayerResult(progress: Long) {
        selectedMedia?.let { media ->
            progressUC.saveProgress(media = media, progress = progress)
            selectedMedia = null
        }
    }

    //endregion

}