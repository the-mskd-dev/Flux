package com.kaem.flux.screens.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.utils.extensions.lastEpisode
import com.kaem.flux.utils.extensions.msToMin
import com.kaem.flux.utils.extensions.timeDescription
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@UnstableApi
@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted mediaId: Long,
    private val artworkRepository: ArtworkRepository,
    private val userRepository: UserRepository,
    settingsRepository: SettingsRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    //region Factory

    @AssistedFactory
    interface Factory {
        fun create(mediaId: Long): PlayerViewModel
    }

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<PlayerEvent>()
    val event = _event.asSharedFlow()

    private val _controlsState = MutableStateFlow(PlayerUiState.Controls())

    val uiState: StateFlow<PlayerUiState> = combine(
        artworkRepository.flow,
        settingsRepository.flow,
        _controlsState,
    ) { artwork, settings, controls ->

        val media = artwork.movie ?: artwork.episodes.find { it.id == mediaId }

        PlayerUiState(
            screen = media?.let { PlayerScreen.Content(media = media) } ?: PlayerScreen.Error,
            playerForward = settings.playerForwardValue.seconds.inWholeMilliseconds,
            playerRewind = settings.playerRewindValue.seconds.inWholeMilliseconds,
            subtitlesLanguage = settings.subtitlesLanguage,
            controls = PlayerUiState.Controls(
                showInterface = controls.showInterface,
                showSettings = controls.showSettings
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerUiState()
    )

    //endregion

    //region Public methods

    fun handleIntent(intent: PlayerIntent) = viewModelScope.launch {
        when (intent) {
            PlayerIntent.ShowInterface -> showInterface()
            PlayerIntent.ShowSettings -> showSettings()
            is PlayerIntent.SaveTime -> saveTime(time = intent.time)
            is PlayerIntent.OnBackTap -> onBackTap(time = intent.time)
            PlayerIntent.TogglePlayButton -> togglePlayButton()
            PlayerIntent.OnFastRewind -> onFastRewind()
            PlayerIntent.OnFastForward -> onFastForward()
            is PlayerIntent.UpdateProgress -> updateProgress(progress = intent.progress)
        }
    }

    //endregion

    //region Private methods

    private suspend fun togglePlayButton() {
        _event.emit(PlayerEvent.TogglePlayButton)
    }

    private suspend fun onFastRewind() {
        _event.emit(PlayerEvent.SeekRewind(uiState.value.playerRewind))
    }

    private suspend fun onFastForward() {
        _event.emit(PlayerEvent.SeekForward(uiState.value.playerForward))
    }

    private suspend fun updateProgress(progress: Long) {
        _event.emit(PlayerEvent.UpdateProgress(progress = progress))
    }

    private fun showInterface() {
        _controlsState.update { it.copy(showInterface = !it.showInterface) }
    }

    private fun showSettings() {
        _controlsState.update { it.copy(showSettings = !it.showSettings) }
    }

    private suspend fun onBackTap(time: Long?) {

        time?.let { saveTime(time = it) }

        val showInterface = uiState.first().controls.showInterface

        if (showInterface) {
            _event.emit(PlayerEvent.BackToPreviousScreen)
        } else {
            showInterface()
        }

    }

    private suspend fun saveTime(time: Long) {

        val media = (uiState.firstOrNull()?.screen as? PlayerScreen.Content)?.media ?: return
        val newStatus = if (time.msToMin >= media.duration * .9) Status.WATCHED else Status.IS_WATCHING
        val newTime = if (newStatus == Status.WATCHED) 0L else time

        val updatedMedia = when (media) {
            is Movie -> media.copy(currentTime = newTime, status = newStatus)
            is Episode -> media.copy(currentTime = newTime, status = newStatus)
        }

        when (updatedMedia) {
            is Movie -> {

                // Add/Remove from recently watched
                if (newStatus == Status.WATCHED) userRepository.removeFromRecentlyWatched(media.artworkId)
                else userRepository.addToRecentlyWatched(media.artworkId)

                // Save in DB
                artworkRepository.saveMovie(updatedMedia)
            }
            is Episode -> {

                // Add/Remove from recently watched
                val episodes = artworkRepository.flow.first().episodes
                val lastEpisode = episodes.lastEpisode
                if (lastEpisode.id == updatedMedia.id && newStatus == Status.WATCHED)
                    userRepository.removeFromRecentlyWatched(media.artworkId)
                else
                    userRepository.addToRecentlyWatched(media.artworkId)

                // Save in DB
                artworkRepository.saveEpisode(updatedMedia)
            }
        }

        Log.i("PlayerViewModel", "${updatedMedia.title} saved at ${time.timeDescription()}")

    }

    //endregion

}