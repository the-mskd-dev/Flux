package com.mskd.flux.screens.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.customization.CustomizationRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.screens.player.PlayerTrack.Type
import com.mskd.flux.screens.player.controllers.PlayerManager
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.utils.extensions.getNextEpisodeFor
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.time.Duration.Companion.seconds


@HiltViewModel(assistedFactory = PlayerViewModel.Factory::class)
class PlayerViewModel @AssistedInject constructor(
    @Assisted mediaId: Long,
    private val artworkUC: ArtworkUC,
    private val settingsRepository: SettingsRepository,
    private val filesRepository: FilesRepository,
    private val playerManager: PlayerManager,
    private val progressUC: ProgressUC,
    private val customizationRepository: CustomizationRepository
) : ViewModel() {

    //region Factory

    @AssistedFactory
    interface Factory {
        fun create(mediaId: Long): PlayerViewModel
    }

    //endregion

    //region Variables

    private val sessionId: String = UUID.randomUUID().toString()

    private var seekResetJob: Job? = null
    private var ambientResetJob: Job? = null

    private var wasPlayingBeforeBackground = false

    //endregion

    //region Flow

    private val _mediaId = MutableStateFlow(mediaId)

    private val _event = Channel<PlayerEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _controlsState = MutableStateFlow(PlayerUiState.Controls())

    private val _tracksState = MutableStateFlow<List<PlayerTrack>>(emptyList())
    private val _seekOverlayState = MutableStateFlow<PlayerUiState.SeekOverlay?>(null)
    private val _ambientOverlayState = MutableStateFlow<PlayerUiState.AmbientOverlay?>(null)

    private val intentChannel = Channel<PlayerIntent>(Channel.UNLIMITED)

    val uiState: StateFlow<PlayerUiState> = combine(
        artworkUC.flow,
        settingsRepository.flow,
        _controlsState,
        _tracksState,
        _seekOverlayState,
        _ambientOverlayState,
        _mediaId,
        playerManager.state,
    ) { flows ->

        val artworkState: State<FullArtwork> = flows[0] as State<FullArtwork>
        val settings = flows[1] as SettingsRepository.State
        val controls = flows[2] as PlayerUiState.Controls
        val tracks = (flows[3] as? List<*>)?.filterIsInstance<PlayerTrack>() ?: emptyList()
        val seekOverlay = flows[4] as PlayerUiState.SeekOverlay?
        val ambientOverlay = flows[5] as PlayerUiState.AmbientOverlay?
        val mediaId = flows[6] as Long
        val playerState = flows[7] as PlayerManager.State

        val media = when (artworkState) {
            is State.Content<FullArtwork> -> {

                when (artworkState.content) {
                    is FullArtwork.FullMovie -> artworkState.content.movie
                    is FullArtwork.FullShow -> artworkState.content.episodes.find { it.id == mediaId }
                }

            }
            else -> null
        }

        val screen: PlayerScreen = when {
            playerState is PlayerManager.State.Error || artworkState is State.Error -> PlayerScreen.Error
            media != null && playerState is PlayerManager.State.Ready -> PlayerScreen.Content(player = playerState.player, media = media)
            else -> PlayerScreen.Loading
        }

        val ready = playerState as? PlayerManager.State.Ready

        PlayerUiState(
            screen = screen,
            playerForward = settings.playerForwardValue,
            playerRewind = settings.playerRewindValue,
            controls = controls.copy(
                isPlaying = ready?.isPlaying ?: false,
                progress = ready?.progress ?: 0L,
                duration = ready?.duration ?: 0L
            ),
            tracks = PlayerUiState.Tracks(
                tracks = tracks,
                selectedAudio = ready?.selectedAudio,
                selectedSubtitles = ready?.selectedSubtitles,
                subtitles = ready?.subtitles ?: emptyList()
            ),
            seekOverlay = seekOverlay,
            ambientOverlay = ambientOverlay,
            waveProgress = customizationRepository.flow.first().waveProgress
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayerUiState()
    )

    //endregion

    //region Lifecycle

    init {
        playerManager.connect(sessionId = sessionId)

        viewModelScope.launch {

            // Play when media and player are available
            launch {
                uiState
                    .map { it.screen }
                    .filterIsInstance<PlayerScreen.Content>()
                    .map { it.media }
                    .distinctUntilChangedBy { it.mediaId }
                    .collect { playMedia(it) }
            }

            // Listen next episode
            launch {
                playerManager.state
                    .filterIsInstance<PlayerManager.State.Ready>()
                    .map { it.showNextEpisode }
                    .distinctUntilChanged()
                    .collect { showNextEpisode(show = it) }
            }

            launch {
                playerManager.state
                    .filterIsInstance<PlayerManager.State.Ready>()
                    .map { it.tracks }
                    .distinctUntilChanged()
                    .collect { updateTracks(tracks = it) }
            }

            launch {
                intentChannel.receiveAsFlow().collect { intent ->
                    processIntent(intent)
                }
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        playerManager.disconnect(sessionId = sessionId)
    }

    //endregion

    //region Public methods

    fun handleIntent(intent: PlayerIntent) {
        intentChannel.trySend(intent)
    }

    //endregion

    //region Private methods

    private suspend fun processIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.PlayMedia -> playMedia(media = intent.media)
            PlayerIntent.ChangeInterfaceVisibility -> changeInterfaceVisibility()
            is PlayerIntent.ShowSettings -> showSettingsSheet(sheet = intent.sheet)
            PlayerIntent.SaveTime -> saveTime()
            PlayerIntent.OnBackTap -> onBackTap()
            PlayerIntent.TogglePlayButton -> togglePlayButton()
            PlayerIntent.OnFastRewind -> onFastRewind()
            PlayerIntent.OnFastForward -> onFastForward()
            is PlayerIntent.UpdateProgress -> updateProgress(progress = intent.progress)
            is PlayerIntent.SelectTrack -> selectTrack(track = intent.track)
            is PlayerIntent.CancelNextEpisode -> cancelNextEpisode()
            is PlayerIntent.PlayNextEpisode -> playNextEpisode(episode = intent.episode)
            is PlayerIntent.OnVolumeChange -> onVolumeChange(delta = intent.delta)
            is PlayerIntent.OnBrightnessChange -> onBrightnessChange(delta = intent.delta)
            is PlayerIntent.UpdateAmbientOverlay -> updateAmbientOverlay(type = intent.type, value = intent.value)
            PlayerIntent.GoToBackground -> onBackground()
            PlayerIntent.GoToForeground -> onForeground()
        }
    }

    private suspend fun playMedia(media: Media) {
        val subtitlesUri = filesRepository.getSubtitlesFor(file = media.file)
        playerManager.playMedia(
            media = media,
            subtitlesUri = subtitlesUri
        )
    }

    private fun togglePlayButton() {
        playerManager.togglePlay()
    }

    private fun onFastRewind() {
        val value = uiState.value.playerRewind
        playerManager.seekRewind(value.seconds.inWholeMilliseconds)
        updateSeekOverlay(type = PlayerUiState.SeekOverlay.Type.REWIND, value = value)

    }

    private fun onFastForward() {
        val value = uiState.value.playerForward
        playerManager.seekForward(value.seconds.inWholeMilliseconds)
        updateSeekOverlay(type = PlayerUiState.SeekOverlay.Type.FORWARD, value = value)
    }

    private fun onVolumeChange(delta: Float) {
        val value = playerManager.changeVolume(delta)
        updateAmbientOverlay(
            type = PlayerUiState.AmbientOverlay.Type.VOLUME,
            value = value
        )
    }

    private suspend fun onBrightnessChange(delta: Float) {
        _event.send(PlayerEvent.ChangeBrightness(delta = delta))
    }

    private fun updateProgress(progress: Long) {
        playerManager.seekTo(progress = progress)
    }

    private fun changeInterfaceVisibility() {
        _controlsState.update { it.copy(showInterface = !it.showInterface) }
    }

    private fun showSettingsSheet(sheet: PlayerUiState.SettingsSheet?) {
        _controlsState.update { it.copy(settingsSheet = sheet) }
    }

    private suspend fun updateTracks(tracks: List<PlayerTrack>) {
        _tracksState.update { listOf(PlayerTrack.NO_SUBTITLES) + tracks }

        val currentSettings = settingsRepository.flow.first()
        val preferredLang = currentSettings.subtitlesLanguage.toPlayerTrack(type = Type.SUBTITLES)

        playerManager.selectTrack(track = preferredLang)

    }

    private suspend fun selectTrack(track: PlayerTrack) {
        playerManager.selectTrack(track = track)

        try {

            if (track.language != null) {
                val locale = Locale.forLanguageTag(track.language)
                if (track.type == Type.SUBTITLES)
                    settingsRepository.setSubtitlesLanguage(locale)
                else
                    settingsRepository.setAudioLanguage(locale)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("PlayerViewModel", "Locale not found for ${track.language}", e)
        }
    }

    private suspend fun showNextEpisode(show: Boolean) {

        val currentEpisode = uiState.first().media as? Episode ?: return

        // If button is canceled, don't show anymore
        if (_controlsState.first().nextButton is PlayerUiState.NextButton.Canceled || currentEpisode.isUnknown)
            return

        if (show) {

            val show = (artworkUC.flow.first() as? State.Content)?.content as? FullArtwork.FullShow
            val episodes = show?.episodes

            val nextEpisode = episodes?.getNextEpisodeFor(currentEpisode) ?: return

            _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Showed(episode = nextEpisode)) }

        } else {

            _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Hidden) }

        }

    }

    private suspend fun playNextEpisode(episode: Episode) {
        saveTime()
        _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Hidden) }
        _mediaId.value = episode.mediaId
    }

    private fun cancelNextEpisode() {
        _controlsState.update { it.copy(nextButton = PlayerUiState.NextButton.Canceled) }
    }

    private suspend fun onBackTap() {

        when (uiState.value.screen) {
            is PlayerScreen.Content -> {

                val interfaceShowed = uiState.value.controls.showInterface

                if (interfaceShowed) {
                    playerManager.pause()
                    saveTime()
                    _event.send(PlayerEvent.BackToPreviousScreen)
                } else {
                    changeInterfaceVisibility()
                }

            }
            else -> {
                _event.send(PlayerEvent.BackToPreviousScreen)
            }
        }

    }

    private suspend fun saveTime() {

        val media = (uiState.value.screen as? PlayerScreen.Content)?.media ?: return
        val progress = uiState.value.controls.progress

        progressUC.saveProgress(media = media, progress = progress)

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

    private fun updateAmbientOverlay(type: PlayerUiState.AmbientOverlay.Type, value: Int) {
        ambientResetJob?.cancel()

        _ambientOverlayState.update {
            PlayerUiState.AmbientOverlay(type = type, value = value)
        }

        ambientResetJob = viewModelScope.launch {
            delay(1000)
            _ambientOverlayState.update { null }
        }
    }

    private suspend fun onBackground() {

        wasPlayingBeforeBackground = uiState.value.controls.isPlaying
        if (wasPlayingBeforeBackground) {
            playerManager.pause()
        }

        saveTime()
    }

    private fun onForeground() {
        if (wasPlayingBeforeBackground) {
            playerManager.play()
        }
    }


    //endregion

}