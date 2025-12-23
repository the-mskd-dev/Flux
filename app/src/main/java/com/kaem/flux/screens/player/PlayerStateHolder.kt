package com.kaem.flux.screens.player

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import com.kaem.flux.utils.extensions.forceScreenOn
import com.kaem.flux.utils.extensions.hideSystemBars
import com.kaem.flux.utils.extensions.setAppInLandscape
import com.kaem.flux.utils.extensions.setAppOrientation
import com.kaem.flux.utils.extensions.showSystemBars
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Stable
class PlayerStateHolder(
    private val viewModel: PlayerViewModel,
    private val activity: ComponentActivity
) {

    val screenState: StateFlow<PlayerScreen> = viewModel.uiState
        .map { it.screen }
        .distinctUntilChanged()
        .stateIn(viewModel.viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerScreen.Loading)

    val interfaceState: StateFlow<PlayerUiState.Interface> = viewModel.uiState
        .map {
            PlayerUiState.Interface(
                isPlaying =  it.isPlaying,
                showInterface = it.showInterface,
                showSettings = it.showSettings
            )
        }
        .distinctUntilChanged()
        .stateIn(viewModel.viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState.Interface())

    val subtitlesState: StateFlow<List<Cue>> = viewModel.uiState
        .map { it.subtitles }
        .distinctUntilChanged()
        .stateIn(viewModel.viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val events = viewModel.event

    fun handleIntent(intent: PlayerIntent) = viewModel.handleIntent(intent)

    val player: Player get() = viewModel.player

    fun setLandscape() {
        activity.setAppInLandscape()
        activity.forceScreenOn(true)
    }

    fun resetOrientation(originalOrientation: Int) {
        activity.setAppOrientation(originalOrientation)
        activity.forceScreenOn(false)
    }

    fun updateSystemBars(show: Boolean) {
        if (show) activity.showSystemBars() else activity.hideSystemBars()
    }
}

@Composable
fun rememberPlayerStateHolder(
    viewModel: PlayerViewModel,
    activity: ComponentActivity = LocalActivity.current as ComponentActivity
): PlayerStateHolder {
    return remember(viewModel, activity) {
        PlayerStateHolder(viewModel, activity)
    }
}