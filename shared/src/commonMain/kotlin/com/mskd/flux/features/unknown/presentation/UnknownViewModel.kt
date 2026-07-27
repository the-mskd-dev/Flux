package com.mskd.flux.features.unknown.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UnknownViewModel(
    observeArtworkUseCase: ObserveArtworkUseCase,
    settingsDataStore: SettingsDataStore,
    private val saveProgress: SaveProgressUseCase
) : ViewModel() {

    //region Variables

    private var selectedMedia: Media? = null

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<UnknownEvent>()
    val event = _event.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<UnknownUiState> = combine(
        observeArtworkUseCase.flow,
        settingsDataStore.flow,
        _searchQuery
    ) { artworkContent, settings, searchQuery ->

        val fullShow = (artworkContent as? State.Content)?.content as? FullArtwork.FullShow
        UnknownUiState(
            screen = State.Content(Unit),
            searchQuery = searchQuery,
            medias = fullShow?.episodes?.sortedWith(
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

    init { observeArtworkUseCase(artworkId = Artwork.UNKNOWN_ID) }

    //endregion

    //region Public Methods

    fun handleIntent(intent: UnknownIntent) = viewModelScope.launch {
        when (intent) {
            is UnknownIntent.PlayMedia -> playMedia(media = intent.media, forceInternal = intent.forceInternal)
            is UnknownIntent.OpenFileExplorer -> _event.emit(UnknownEvent.OpenFileExplorer(media = intent.media))
            UnknownIntent.OnBackTap -> _event.emit(UnknownEvent.BackToPreviousScreen)
            UnknownIntent.OnInfoTap -> _event.emit(UnknownEvent.NavigateToHowToScreen)
            is UnknownIntent.OnExternalPlayerResult -> onExternalPlayerResult(progress = intent.progress)
            is UnknownIntent.DoSearch -> doSearch(query = intent.query)
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
            saveProgress(media = media, progress = progress)
            selectedMedia = null
        }
    }

    private fun doSearch(query: String) {
        _searchQuery.update { query }
    }

    //endregion

}