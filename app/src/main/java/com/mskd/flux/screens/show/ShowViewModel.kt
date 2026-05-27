package com.mskd.flux.screens.show

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.screens.artwork.ArtworkEvent
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


    //endregion

    //region Init

    init {

        artworkUC.searchArtwork(artworkId = artworkId)

        viewModelScope.launch {
            artworkUC.flow.collect { artworkState ->
                _uiState.update { it.copy(state = artworkState) }
            }
        }

    }

    //endregion

}