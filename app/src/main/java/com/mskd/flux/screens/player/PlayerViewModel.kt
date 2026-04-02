package com.mskd.flux.screens.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.getNextEpisodeFor
import com.mskd.flux.utils.extensions.lastEpisode
import com.mskd.flux.utils.extensions.timeDescription
import com.mskd.flux.utils.extensions.toPlayerTrack
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted mediaId: Long,
    private val artworkRepository: ArtworkRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    //region Factory

    @AssistedFactory
    interface Factory {
        fun create(mediaId: Long): PlayerViewModel
    }

    //endregion

    //region Variables

    private var seekResetJob: Job? = null

    //endregion

    //region Flow

    private val _mediaId = MutableStateFlow(mediaId)

    private val _event = Channel<PlayerEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _controlsState = MutableStateFlow(PlayerUiState.Controls())
    private val _tracksState = MutableStateFlow(PlayerUiState.Tracks())
    private val _seekOverlayState = MutableStateFlow<PlayerUiState.SeekOverlay?>(null)

    val uiState: StateFlow<PlayerUiState> = combine(
        artworkRepository.flow,
        settingsRepository.flow,
        _controlsState,
        _tracksState,
        _seekOverlayState,
        _mediaId,
    ) { flows ->

        val artwork = flows[0] as ArtworkRepository.State
        val settings = flows[1] as SettingsRepository.State
        val controls = flows[2] as PlayerUiState.Controls
        val tracks = flows[3] as PlayerUiState.Tracks
        val seekOverlay = flows[4] as PlayerUiState.SeekOverlay?
        val id = flows[5] as Long

        val media = artwork.movie ?: artwork.episodes.find { it.id == id }

        PlayerUiState(
            screen = media?.let { PlayerScreen.Content(media = media) } ?: PlayerScreen.Error,
            playerForward = settings.playerForwardValue,
            playerRewind = settings.playerRewindValue,
            controls = controls,
            tracks = tracks,
            seekOverlay = seekOverlay
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
            PlayerIntent.ChangeInterfaceVisibility -> changeInterfaceVisibility()
            is PlayerIntent.ShowSettings -> showSettingsSheet(sheet = intent.sheet)
            is PlayerIntent.SaveTime -> saveTime(time = intent.time)
            is PlayerIntent.OnBackTap -> onBackTap(time = intent.time)
            PlayerIntent.TogglePlayButton -> togglePlayButton()
            is PlayerIntent.SetPlayingStatus -> setPlayingStatus(isPlaying = intent.isPlaying)
            PlayerIntent.OnFastRewind -> onFastRewind()
            PlayerIntent.OnFastForward -> onFastForward()
            is PlayerIntent.UpdateProgress -> updateProgress(progress = intent.progress)
            is PlayerIntent.UpdateTracks -> updateTracks(tracks = intent.tracks)
            is PlayerIntent.SelectTrack -> selectTrack(track = intent.track)
            is PlayerIntent.OnTrackSelected -> onTrackSelected(track = intent.track)
            is PlayerIntent.ShowNextEpisode -> showNextEpisode(show = intent.show)
            is PlayerIntent.CancelNextEpisode -> cancelNextEpisode()
            is PlayerIntent.PlayNextEpisode -> playNextEpisode(episode = intent.episode)
        }
    }

    //endregion

    //region Private methods

    private suspend fun togglePlayButton() {
        _event.send(PlayerEvent.TogglePlayButton)
    }

    private fun setPlayingStatus(isPlaying: Boolean) {
        _controlsState.update { it.copy(isPlaying = isPlaying) }
    }

    private suspend fun onFastRewind() {
        val value = uiState.value.playerRewind
        _event.send(PlayerEvent.SeekRewind(value.seconds.inWholeMilliseconds))
        updateSeekOverlay(type = PlayerUiState.SeekOverlay.Type.REWIND, value = value)

    }

    private suspend fun onFastForward() {
        val value = uiState.value.playerForward
        _event.send(PlayerEvent.SeekForward(value.seconds.inWholeMilliseconds))
        updateSeekOverlay(type = PlayerUiState.SeekOverlay.Type.FORWARD, value = value)
    }

    private suspend fun updateProgress(progress: Long) {
        _event.send(PlayerEvent.UpdateProgress(progress = progress))
    }

    private fun changeInterfaceVisibility() {
        _controlsState.update { it.copy(showInterface = !it.showInterface) }
    }

    private fun showSettingsSheet(sheet: PlayerUiState.SettingsSheet?) {
        _controlsState.update { it.copy(settingsSheet = sheet) }
    }

    private suspend fun updateTracks(tracks: List<PlayerTrack>) {
        _tracksState.update { it.copy(tracks = tracks) }

        val currentSettings = settingsRepository.flow.first()
        val preferredLang = currentSettings.subtitlesLanguage.toPlayerTrack(type = PlayerTrack.Type.SUBTITLES)

        _event.send(PlayerEvent.SelectTrack(preferredLang))

    }

    private suspend fun selectTrack(track: PlayerTrack) {
        _event.send(PlayerEvent.SelectTrack(track = track))

        try {

            if (track.language != null) {
                val locale = Locale.forLanguageTag(track.language)
                if (track.type == PlayerTrack.Type.SUBTITLES)
                    settingsRepository.setSubtitlesLanguage(locale)
                else
                    settingsRepository.setAudioLanguage(locale)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PlayerViewModel", "Locale not found for ${track.language}", e)
        }
    }

    private fun onTrackSelected(track: PlayerTrack) {

        _tracksState.update {
            when (track.type) {
                PlayerTrack.Type.AUDIO -> it.copy(selectedAudio = track)
                PlayerTrack.Type.SUBTITLES -> it.copy(selectedSubtitles = track)
            }
        }

    }

    private suspend fun showNextEpisode(show: Boolean) {

        val currentEpisode = uiState.first().media as? Episode ?: return

        // If button is canceled, don't show anymore
        if (_controlsState.first().nextButton is PlayerUiState.NextButton.Canceled)
            return

        if (show) {

            val episodes = artworkRepository.flow.first().episodes
            val nextEpisode = episodes.getNextEpisodeFor(currentEpisode) ?: return

            _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Showed(episode = nextEpisode)) }

        } else {

            _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Hidden) }

        }

    }

    private suspend fun playNextEpisode(episode: Episode) {
        _event.send(PlayerEvent.SaveTimeRequested)
        _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Hidden) }
        _mediaId.value = episode.mediaId
    }

    private fun cancelNextEpisode() {
        _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Canceled) }
    }

    private suspend fun onBackTap(time: Long?) {

        val interfaceShowed = uiState.value.controls.showInterface

        if (interfaceShowed) {
            time?.let { saveTime(time = it) }
            _event.send(PlayerEvent.BackToPreviousScreen)
        } else {
            changeInterfaceVisibility()
        }

    }

    private suspend fun saveTime(time: Long) {

        val media = (uiState.value.screen as? PlayerScreen.Content)?.media ?: return
        val newStatus = if (time >= (media.duration * Constants.PLAYER.PROGRESS_THRESHOLD).minutes.inWholeMilliseconds) Status.WATCHED else Status.IS_WATCHING
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

    private fun updateSeekOverlay(type: PlayerUiState.SeekOverlay.Type, value: Int) {
        seekResetJob?.cancel()

        _seekOverlayState.update { state ->
            val amount = if (state?.type == type) state.amount + value else value
            PlayerUiState.SeekOverlay(type = type, amount = amount)
        }

        seekResetJob = viewModelScope.launch {
            delay(2000)
            _seekOverlayState.update { null }
        }

    }

    //endregion

}