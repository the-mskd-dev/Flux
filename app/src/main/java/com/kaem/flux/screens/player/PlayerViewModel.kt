package com.kaem.flux.screens.player

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.kaem.flux.data.repository.ArtworkRepository
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.UserRepository
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

    //region Player

    private val _player : Player = ExoPlayer.Builder(context)
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

    private val _interfaceState = MutableStateFlow(PlayerUiState.Interface())
    private val _subtitlesState = MutableStateFlow(PlayerUiState.Subtitles())

    val uiState: StateFlow<PlayerUiState> = combine(
        artworkRepository.flow,
        settingsRepository.flow,
        _interfaceState,
        _subtitlesState
    ) { artwork, settings, interfaceState, subtitlesState ->

        val media = artwork.movie ?: artwork.episodes.find { it.id == mediaId }
        playMedia(media = media)
        selectSubtitles(settings.subtitlesLanguage.language)

        PlayerUiState(
            screen = media?.let { PlayerScreen.Content(media = media) } ?: PlayerScreen.Error,
            playerForward = settings.playerForwardValue.seconds.inWholeMilliseconds,
            playerRewind = settings.playerRewindValue.seconds.inWholeMilliseconds,
            subtitlesLanguage = settings.subtitlesLanguage,
            isPlaying = interfaceState.isPlaying,
            showInterface = interfaceState.showInterface,
            subtitles = subtitlesState.subtitles
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

    private fun togglePlayButton() {
        if (_player.isPlaying) player.pause() else _player.play()
    }

    private fun onFastRewind() {
        _player.seekTo(_player.currentPosition - uiState.value.playerRewind)
    }

    private fun onFastForward() {
        _player.seekTo(_player.currentPosition + uiState.value.playerForward)
    }

    private fun updateProgress(progress: Long) {
        _player.seekTo(progress)
    }

    private fun showInterface() {
        _interfaceState.update { it.copy(showInterface = !it.showInterface) }
    }

    private fun showSettings() {
        _interfaceState.update { it.copy(showSettings = !it.showSettings) }
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

    private fun playMedia(media: Media?) {

        val currentMedia = (uiState.value.screen as? PlayerScreen.Content)?.media

        if (media != null && media.mediaId != currentMedia?.mediaId) {
            _player.setMediaItem(MediaItem.fromUri(media.file.path.toUri()))
            _player.seekTo(media.currentTime)
            _player.prepare()
        }

    }

    private fun selectSubtitles(language: String) {

        val currentLang = player.trackSelectionParameters.preferredTextLanguages.firstOrNull()
        if (currentLang != language) {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setPreferredTextLanguage(language)
                .build()
        }

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
                _interfaceState.update {
                    it.copy(isPlaying = player.playWhenReady)
                }
            }
        }

        override fun onCues(cueGroup: androidx.media3.common.text.CueGroup) {
            _subtitlesState.update {
                it.copy(subtitles = cueGroup.cues)
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