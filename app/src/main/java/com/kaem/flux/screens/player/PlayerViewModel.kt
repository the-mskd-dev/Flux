package com.kaem.flux.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.MediaRepository
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.model.media.Media
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted private val media: Media,
    private val repository: MediaRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(media: Media): PlayerViewModel
    }

    val uiState: StateFlow<PlayerUiState> = settingsRepository.flow.map { settings ->
        PlayerUiState(
            state = PlayerScreenState.Content(media = media),
            playerForward = settings.playerForwardValue.seconds.inWholeMilliseconds,
            playerBackward = settings.playerBackwardValue.seconds.inWholeMilliseconds,
            subtitlesLanguage = settings.subtitlesLanguage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerUiState()
    )


}