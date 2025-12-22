package com.kaem.flux.screens.player

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.UserRepository
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Media
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@UnstableApi
@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted private val media: Media,
    private val repository: ArtworkRepository,
    private val userRepository: UserRepository,
    settingsRepository: SettingsRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    //region Factory

    @AssistedFactory
    interface Factory {
        fun create(media: Media): PlayerViewModel
    }

    //endregion

    //region Player

    private val _player = ExoPlayer.Builder(context)
        .setRenderersFactory(
            DefaultRenderersFactory(context)
                .setExtensionRendererMode(
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                )
        )
        .build()
        .apply {
            playWhenReady = true
            setSeekParameters(SeekParameters.CLOSEST_SYNC)
        }
    val player = _player

    //endregion

    //region Flow

    private val _event = MutableSharedFlow<PlayerEvent>()
    val event = _event.asSharedFlow().distinctUntilChanged()

    private val _subState = MutableStateFlow(PlayerUiState.SubState())


    val uiState: StateFlow<PlayerUiState> = combine(
        settingsRepository.flow,
        _subState
    ) { settings, subState ->
        PlayerUiState(
            screen = ScreenState.CONTENT,
            isPlaying = subState.isPlaying,
            showInterface = subState.showInterface,
            playerForward = settings.playerForwardValue.seconds.inWholeMilliseconds,
            playerRewind = settings.playerRewindValue.seconds.inWholeMilliseconds,
            subtitlesLanguage = settings.subtitlesLanguage
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
            is PlayerIntent.ShowInterface -> showInterface()
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

    private fun togglePlayButton() {
        if (_player.isPlaying) player.pause() else _player.play()
    }

    private fun onFastRewind() {
        _player.seekTo(_player.currentPosition - uiState.value.playerRewind)
    }

    private fun onFastForward() {
        _player.seekTo(_player.currentPosition + uiState.value.playerForward)
    }

    private fun showInterface() {
        _subState.update { it.copy(showInterface = !it.showInterface) }
    }

    private fun updateProgress(progress: Long) {
        _player.seekTo(progress)
    }

    private suspend fun onBackTap(time: Long?) {

        time?.let { saveTime(time = it) }

        val showInterface = uiState.first().showInterface

        if (showInterface) {
            _event.emit(PlayerEvent.BackToPreviousScreen)
        } else {
            showInterface()
        }

    }

    private suspend fun saveTime(time: Long) {

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
                repository.saveMovie(updatedMedia)
            }
            is Episode -> {

                // Add/Remove from recently watched
                val episodes = repository.flow.first().episodes
                val lastEpisode = episodes.lastEpisode
                if (lastEpisode.id == updatedMedia.id && newStatus == Status.WATCHED)
                    userRepository.removeFromRecentlyWatched(media.artworkId)
                else
                    userRepository.addToRecentlyWatched(media.artworkId)

                // Save in DB
                repository.saveEpisode(updatedMedia)
            }
        }

        Log.i("PlayerViewModel", "${updatedMedia.title} saved at ${time.timeDescription()}")

    }

    //endregion

    //region Listener

    private val playerListener = object : Player.Listener {

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED
                )
            ) {
                _subState.update {
                    it.copy(isPlaying = player.playWhenReady)
                }
            }
        }

    }

    //endregion

    //region Lifecycle

    init {
        _player.addListener(playerListener)
    }

    override fun onCleared() {
        super.onCleared()
        _player.removeListener(playerListener)
    }

    //endregion

}