package com.mskd.flux.screen.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.model.State
import com.mskd.flux.model.StringProvider
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.player.PlayerTrack
import com.mskd.flux.model.player.PlayerTrack.Type
import com.mskd.flux.platform.PlayerManager
import com.mskd.flux.screen.player.PlayerUiContent.AmbientOverlay
import com.mskd.flux.screen.player.PlayerUiContent.NextButton
import com.mskd.flux.screen.player.PlayerUiContent.SeekOverlay
import com.mskd.flux.screen.player.PlayerUiContent.SettingsSheet
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.player.PipIsEnabledUC
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.getNextEpisodeFor
import com.mskd.flux.utils.extensions.toPlayerTrack
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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


class PlayerViewModel<out T>(
    mediaId: Long,
    private val artworkUC: ArtworkUC,
    private val settingsRepository: SettingsRepository,
    private val filesRepository: FilesRepository,
    private val playerManager: PlayerManager<T>,
    private val progressUC: ProgressUC,
    private val pipIsEnabledUC: PipIsEnabledUC
) : ViewModel() {

    //region Variables

    private val sessionId: String = UUID.randomUUID().toString()

    private var seekResetJob: Job? = null
    private var ambientResetJob: Job? = null

    private var wasPlayingBeforeBackground = false

    private val content get() = (uiState.value.state as? State.Content<PlayerUiContent<T>>)?.content

    //endregion

    //region Flow

    private val intentChannel = Channel<PlayerIntent>(Channel.UNLIMITED)

    private val _event = Channel<PlayerEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val _userState = MutableStateFlow(PlayerUserState(mediaId = mediaId))

    private val _subtitles = MutableStateFlow<List<String?>>(emptyList())
    val subtitles: StateFlow<List<String?>> = _subtitles.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    val uiState: StateFlow<PlayerUiState<T>> = combine(
        artworkUC.flow,
        settingsRepository.flow,
        playerManager.flow,
        _userState,
    ) { artworkState, settings, playerState, userState ->

        when {
            artworkState is State.Error -> {
                PlayerUiState(state = State.Error())
            }
            playerState is PlayerManager.State.Error -> {
                val (code, message) = playerState
                PlayerUiState(state = State.Error(code = code, message = message?.let { StringProvider.Static(it) }))
            }
            artworkState !is State.Content || playerState !is PlayerManager.State.Ready ->
                PlayerUiState(state = State.Loading)
            else -> {

                val media = resolveMedia(
                    fullArtwork = artworkState.content,
                    mediaId = userState.mediaId
                ) ?: return@combine PlayerUiState(state = State.Error())

                val dataState = PlayerDataState(
                    fullArtwork = artworkState.content,
                    media = media,
                    player = playerState.player,
                    playerRewind = settings.playerRewindValue,
                    playerForward = settings.playerForwardValue,
                    duration = playerState.duration,
                    tracks = listOf(PlayerTrack.NO_SUBTITLES) + playerState.tracks,
                    isPlaying = playerState.isPlaying,
                    selectedAudio = playerState.selectedAudio,
                    selectedSubtitles = playerState.selectedSubtitles,
                )

                PlayerUiState<T>(state = mergeStates(dataState, userState))
            }
        }

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
                    .map { it.state }
                    .filterIsInstance<State.Content<PlayerUiContent<T>>>()
                    .map { it.content.media }
                    .distinctUntilChangedBy { it.mediaId }
                    .collect { playMedia(it) }
            }

            // Auto-select preferred language when tracks change
            launch {
                playerManager.flow
                    .filterIsInstance<PlayerManager.State.Ready<T>>()
                    .map { it.tracks }
                    .distinctUntilChanged()
                    .collect { updateTracks() }
            }

            // Show/hide next episode button
            launch {
                playerManager.progress
                    .map { it.showNextEpisode }
                    .distinctUntilChanged()
                    .collect { showNextEpisode(show = it) }
            }

            // Update progress
            launch {
                playerManager.progress
                    .map { it.progress }
                    .distinctUntilChanged()
                    .collect { progress -> _progress.update { progress } }
            }

            // Update subtitles
            launch {
                playerManager.subtitles
                    .distinctUntilChanged()
                    .collect { subtitles ->
                        Trace.debug(message = "new subtitles : $subtitles")
                        _subtitles.update { subtitles }
                    }
            }

            // Process intents
            launch {
                intentChannel.receiveAsFlow().collect { intent ->
                    processIntent(intent)
                }
            }
        }

    }

    override fun onCleared() {
        playerManager.disconnect(sessionId = sessionId)
    }

    //endregion

    //region Public methods

    fun handleIntent(intent: PlayerIntent) {
        intentChannel.trySend(intent)
    }

    //endregion

    //region Private methods

    private fun resolveMedia(fullArtwork: FullArtwork, mediaId: Long) : Media? {
        return when (fullArtwork) {
            is FullArtwork.FullMovie -> fullArtwork.movie
            is FullArtwork.FullShow -> fullArtwork.episodes.find { it.mediaId == mediaId }
        }
    }

    private fun mergeStates(dataState: PlayerDataState<T>, userState: PlayerUserState) : State<PlayerUiContent<T>> {
        return State.Content(
            content = PlayerUiContent(
                fullArtwork = dataState.fullArtwork,
                media = dataState.media,
                playerRewind = dataState.playerRewind,
                playerForward = dataState.playerForward,
                player = dataState.player,
                isPlaying = dataState.isPlaying,
                duration = dataState.duration,
                tracks = dataState.tracks,
                selectedAudio = dataState.selectedAudio,
                selectedSubtitles = dataState.selectedSubtitles,
                showInterface = userState.showInterface,
                isInPip = userState.isInPip,
                seekOverlay = userState.seekOverlay,
                ambientOverlay = userState.ambientOverlay,
                settingsSheet = userState.settingsSheet,
                nextButton = userState.nextButton,
            )
        )
    }

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
            is PlayerIntent.OnPipChange -> onPipChange(isInPip = intent.isInPip)
            PlayerIntent.OnClosePiP -> onClosePip()
        }
    }

    private suspend fun playMedia(media: Media) {
        val subtitlesUri = filesRepository.getSubtitlesFor(file = media.file)
        playerManager.playMedia(
            media = media,
            subtitlesPath = subtitlesUri?.absolutePath
        )
    }

    private fun togglePlayButton() {
        playerManager.togglePlay()
    }

    private fun onFastRewind() {
        val value = content?.playerRewind ?: return
        playerManager.seekRewind(value.seconds.inWholeMilliseconds)
        updateSeekOverlay(type = SeekOverlay.Type.REWIND, value = value)

    }

    private fun onFastForward() {
        val value = content?.playerForward ?: return
        playerManager.seekForward(value.seconds.inWholeMilliseconds)
        updateSeekOverlay(type = SeekOverlay.Type.FORWARD, value = value)
    }

    private fun onVolumeChange(delta: Float) {
        val value = playerManager.changeVolume(delta)
        updateAmbientOverlay(
            type = AmbientOverlay.Type.VOLUME,
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
        _userState.update { it.copy(showInterface = !it.showInterface) }
    }

    private fun showSettingsSheet(sheet: SettingsSheet?) {
        _userState.update { it.copy(settingsSheet = sheet) }
    }

    private suspend fun updateTracks() {
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
            Trace.error("PlayerViewModel", "Locale not found for ${track.language}", e)
        }
    }

    private suspend fun showNextEpisode(show: Boolean) {

        val currentEpisode = content?.media as? Episode ?: return

        // If button is canceled, don't show anymore
        if (_userState.first().nextButton is NextButton.Canceled || currentEpisode.isUnknown)
            return

        if (show) {

            val show = (artworkUC.flow.first() as? State.Content)?.content as? FullArtwork.FullShow
            val episodes = show?.episodes?.filter { it.season == currentEpisode.season }

            val nextEpisode = episodes?.getNextEpisodeFor(currentEpisode) ?: return

            _userState.update { it.copy(nextButton = NextButton.Showed(episode = nextEpisode)) }

        } else {

            _userState.update { it.copy(nextButton = NextButton.Hidden) }

        }

    }

    private suspend fun playNextEpisode(episode: Episode) {
        saveTime()
        _userState.update {
            it.copy(
                nextButton = NextButton.Hidden,
                mediaId = episode.mediaId
            )
        }

    }

    private fun cancelNextEpisode() {
        _userState.update { it.copy(nextButton = NextButton.Canceled) }
    }

    private suspend fun onBackTap() {

        val content = content

        when {
            content == null -> {
                _event.send(PlayerEvent.BackToPreviousScreen)
            }
            content.showInterface -> {
                playerManager.pause()
                saveTime()
                _event.send(PlayerEvent.BackToPreviousScreen)
            }
            else -> {
                changeInterfaceVisibility()
            }
        }

    }

    private suspend fun saveTime() {
        val media = content?.media ?: return
        val progress = _progress.value

        progressUC.saveProgress(
            media = media,
            progress = progress
        )

    }

    private fun updateSeekOverlay(type: SeekOverlay.Type, value: Int) {
        seekResetJob?.cancel()

        _userState.update { state ->
            val current = state.seekOverlay
            val amount = if (current?.type == type) current.amount + value else value
            state.copy(seekOverlay = SeekOverlay(type = type, amount = amount))
        }

        seekResetJob = viewModelScope.launch {
            delay(1.seconds)
            _userState.update { it.copy(seekOverlay = null) }
        }
    }

    private fun updateAmbientOverlay(type: AmbientOverlay.Type, value: Int) {
        ambientResetJob?.cancel()

        _userState.update {
            it.copy(ambientOverlay = AmbientOverlay(type = type, value = value))
        }

        ambientResetJob = viewModelScope.launch {
            delay(1.seconds)
            _userState.update {
                it.copy(ambientOverlay = null)
            }
        }
    }

    private suspend fun onBackground() {

        if (pipIsEnabledUC()) return

        wasPlayingBeforeBackground = content?.isPlaying ?: return

        if (wasPlayingBeforeBackground)
            playerManager.pause()

        saveTime()
    }

    private fun onForeground() {
        if (wasPlayingBeforeBackground) {
            playerManager.play()
        }
    }

    private fun onPipChange(isInPip: Boolean) {
        _userState.update { it.copy(isInPip = isInPip) }
    }

    private suspend fun onClosePip() {
        playerManager.pause()
        saveTime()
    }

    //endregion

}