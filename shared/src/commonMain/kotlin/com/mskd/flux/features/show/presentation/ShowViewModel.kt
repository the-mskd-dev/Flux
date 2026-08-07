package com.mskd.flux.features.show.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShowViewModel(
    private val artworkId: Long,
    observeArtworkUseCase: ObserveArtworkUseCase,
    private val resetProgress: ResetProgressUseCase,
) : ViewModel() {

    //region Flow

    private val _event = MutableSharedFlow<ShowEvent>()
    val event = _event.asSharedFlow()

    private val _userState = MutableStateFlow<ShowDialog?>(null)

    val uiState: StateFlow<ShowUiState> = combine(
        observeArtworkUseCase.flow,
        _userState,
    ) { artworkState, dialog ->
        when (artworkState) {
            is State.Loading -> ShowUiState(state = State.Loading)
            is State.Error -> ShowUiState(state = State.Error())
            is State.Content -> {
                val fullShow = artworkState.content as? FullArtwork.FullShow
                    ?: return@combine ShowUiState(state = State.Error())
                ShowUiState(
                    state = State.Content(
                        ShowContent(fullShow = fullShow, dialog = dialog)
                    )
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShowUiState()
    )

    //endregion

    //region Computed properties

    private val showContent get() = (uiState.value.state as? State.Content)?.content

    //endregion

    //region Init

    init {
        observeArtworkUseCase(artworkId = artworkId)
    }

    //endregion

    //region Public Methods

    fun handleIntent(intent: ShowIntent) = viewModelScope.launch {
        when (intent) {
            // Navigation
            ShowIntent.OnBackTap -> _event.emit(ShowEvent.BackToPreviousScreen)
            is ShowIntent.OnSeasonClick -> navigateToSeason(season = intent.season, rgb = intent.rgb)

            // Dialogs
            ShowIntent.CloseDialog -> closeDialog()
            is ShowIntent.ShowSeasonPreview -> showSeasonPreview(season = intent.season)
            is ShowIntent.ShowResetProgressDialog -> showResetDialog()

            // Other
            ShowIntent.OpenShowInfo -> openShowInfo()
            ShowIntent.ResetProgress -> resetProgress()
        }
    }

    //endregion

    //region Private Methods

    private suspend fun navigateToSeason(season: Int, rgb: Int?) {
        _event.emit(ShowEvent.NavigateToSeason(artworkId = artworkId, season = season, rgb = rgb))
    }

    private fun closeDialog() {
        _userState.update { null }
    }

    private fun showSeasonPreview(season: Season) {
        _userState.update { ShowDialog.SeasonPreview(season = season) }
    }

    private fun showResetDialog() {
        _userState.update { ShowDialog.ResetProgress }
    }

    private suspend fun openShowInfo() {
        val fullShow = showContent?.fullShow ?: return

        _event.emit(ShowEvent.OpenShowInfo(url = fullShow.artwork.infoUrl))
    }

    private suspend fun resetProgress() {
        val fullShow = showContent?.fullShow ?: return

        resetProgress(artwork = fullShow.artwork, season = null)
        _userState.update { null }
    }

    //endregion
}