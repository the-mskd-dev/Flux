package com.mskd.flux.screens.show

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.artwork.ArtworkEvent
import com.mskd.flux.screens.artwork.ArtworkEvent.OpenEpisodeInfo
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.screens.artwork.ArtworkUiState
import com.mskd.flux.screens.artwork.ArtworkViewModel
import com.mskd.flux.screens.artwork.ArtworkViewModel.UserState
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.progress.ProgressUC
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ShowViewModel.Factory::class)
class ShowViewModel @AssistedInject constructor(
    @Assisted val artworkId: Long,
    private val artworkUC: ArtworkUC,
    private val settingsRepository: SettingsRepository,
    private val progressUC: ProgressUC
) : ViewModel() {

    //region Hilt

    @AssistedFactory
    interface Factory {
        fun create(artworkId: Long): ShowViewModel
    }

    //endregion

    //region Flow

    private val _uiState: MutableStateFlow<ShowUiState> = MutableStateFlow(ShowUiState())
    val uiState: StateFlow<ShowUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ShowEvent>()
    val event = _event.asSharedFlow()


    //endregion

    //region Init

    init {

        artworkUC.searchArtwork(artworkId = artworkId)

        viewModelScope.launch {
            artworkUC.flow.collect { artworkState ->
                _uiState.update {

                    val state = when (artworkState) {
                        is State.Content<FullArtwork> -> (it.state as? State.Content<FullArtwork>)?.copy(content = artworkState.content) ?: artworkState
                        State.Error -> State.Error
                        State.Loading -> State.Loading
                    }

                    it.copy(state = state)

                }
            }
        }

    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: ShowIntent) = viewModelScope.launch {
        when (intent) {
            // Navigation
            ShowIntent.OnBackTap -> _event.emit(ShowEvent.BackToPreviousScreen)
            is ShowIntent.OnSeasonTap -> navigateToSeason(season = intent.season, rgb = intent.rgb)

            // Dialogs
            ShowIntent.CloseDialog -> closeDialog()
            is ShowIntent.ShowSeasonPreview -> showSeasonPreview(season = intent.season)
        }
    }

    //endregion

    //region Private Methods

    private suspend fun navigateToSeason(season: Int, rgb: Int?) {
        _event.emit(ShowEvent.NavigateToSeason(artworkId = artworkId, season = season, rgb = rgb))
    }

    private fun showSeasonPreview(season: Season) {
        _uiState.update { it.copy(dialog = ShowDialog.SeasonPreview(season = season)) }
    }

    private fun closeDialog() {
        _uiState.update { it.copy(dialog = null) }
    }

    //endregion
}