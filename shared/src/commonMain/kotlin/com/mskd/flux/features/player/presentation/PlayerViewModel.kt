package com.mskd.flux.features.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.core.model.player.PlayerTrack.Type
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import com.mskd.flux.features.player.data.PipIsEnabledUseCase
import com.mskd.flux.features.player.domain.manager.PlayerManager
import com.mskd.flux.features.player.domain.manager.SeekPrecision
import com.mskd.flux.features.player.domain.mapper.toPlayerTrack
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.model.VideoChapterType
import com.mskd.flux.features.player.domain.model.currentAt
import com.mskd.flux.features.player.domain.usecase.GetChaptersUseCase
import com.mskd.flux.features.player.domain.usecase.GetNextEpisodeUseCase
import com.mskd.flux.features.player.domain.usecase.SaveTrackLanguageUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.ui.text.TextProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid


class PlayerViewModel<out T>(
    params: PlayerParams,
    private val settingsDataStore: SettingsDataStore,
    private val playerManager: PlayerManager<T>,
    private val observeArtworkUseCase: ObserveArtworkUseCase,
    private val pipIsEnabledUseCase: PipIsEnabledUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val getSubtitlesUseCase: GetSubtitlesUseCase,
    private val saveTrackLanguageUseCase: SaveTrackLanguageUseCase,
    private val getChaptersUseCase: GetChaptersUseCase,
    private val getNextEpisodeUseCase: GetNextEpisodeUseCase
) : ViewModel() {

    //region Variables

    private val sessionId: String = Uuid.random().toString()

    private var seekResetJob: Job? = null

    private var wasPlayingBeforeBackground = false

    private val currentSession get() = (session.value as? State.Content<PlayerSession<T>>)?.content

    private var chapters: List<VideoChapter> = emptyList()

    // Next episode lookup, cached for the current media (null result included)
    private var nextEpisodeCache: Pair<Long, Episode?>? = null

    //endregion

    //region Flow

    private val intentChannel = Channel<PlayerIntent>(Channel.UNLIMITED)

    private val _event = Channel<PlayerEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    private val mediaId = MutableStateFlow(params.mediaId)

    // Volume actually applied to the player, for display
    private val _volume = MutableSharedFlow<Int>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val volume: SharedFlow<Int> = _volume.asSharedFlow()

    private val _interface = MutableStateFlow(PlayerInterfaceState())
    val interfaceState: StateFlow<PlayerInterfaceState> = _interface.asStateFlow()

    private val _overlays = MutableStateFlow(PlayerOverlays())
    val overlays: StateFlow<PlayerOverlays> = _overlays.asStateFlow()

    private val _subtitles = MutableStateFlow<List<String?>>(emptyList())
    val subtitles: StateFlow<List<String?>> = _subtitles.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    // The full Artwork is only used to resolve the Media: only a Media change is propagated
    private val mediaState: Flow<State<Media>> = combine(
        observeArtworkUseCase(artworkId = params.artworkId),
        mediaId,
    ) { artworkState, mediaId ->
        when (artworkState) {
            is State.Content -> resolveMedia(fullArtwork = artworkState.content, mediaId = mediaId)
                ?.let { State.Content(it) }
                ?: State.Error()
            is State.Error -> State.Error()
            else -> State.Loading
        }
    }.distinctUntilChanged { old, new ->
        old is State.Content && new is State.Content && old.content.mediaId == new.content.mediaId
    }

    val isPlaying: StateFlow<Boolean> = playerManager.flow
        .map { (it as? PlayerManager.State.Ready)?.isPlaying == true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val session: StateFlow<State<PlayerSession<T>>> = combine(
        mediaState,
        settingsDataStore.flow,
        playerManager.flow,
    ) { mediaState, settings, playerState ->

        when {
            mediaState is State.Error -> State.Error()
            playerState is PlayerManager.State.Error -> {
                val (code, message) = playerState
                State.Error(code = code, message = message?.let { TextProvider.Static(it) })
            }
            mediaState !is State.Content || playerState !is PlayerManager.State.Ready -> State.Loading
            else -> State.Content(
                PlayerSession(
                    media = mediaState.content,
                    player = playerState.player,
                    playerRewind = settings.playerRewindValue,
                    playerForward = settings.playerForwardValue,
                    duration = playerState.duration,
                    tracks = listOf(PlayerTrack.NO_SUBTITLES) + playerState.tracks,
                    selectedAudio = playerState.selectedAudio,
                    selectedSubtitles = playerState.selectedSubtitles,
                )
            )
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = State.Loading
    )

    //endregion

    //region Lifecycle

    init {

        playerManager.connect(sessionId = sessionId)

        viewModelScope.launch {

            // Play when media and player are available
            launch {
                session
                    .filterIsInstance<State.Content<PlayerSession<T>>>()
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

            // Update progress
            launch {
                playerManager.progress
                    .distinctUntilChanged()
                    .collect { listenProgress(it) }
            }

            // Update subtitles
            launch {
                playerManager.subtitles
                    .distinctUntilChanged()
                    .collect { subtitles ->
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

    private suspend fun processIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.ChangeInterfaceVisibility -> changeInterfaceVisibility()
            is PlayerIntent.ShowSettings -> showSettingsSheet(sheet = intent.sheet)
            PlayerIntent.OnBackTap -> onBackTap()
            PlayerIntent.TogglePlayButton -> togglePlayButton()
            PlayerIntent.OnFastRewind -> onFastRewind()
            PlayerIntent.OnFastForward -> onFastForward()
            is PlayerIntent.SeekTo -> seekTo(progress = intent.progress, precision = intent.precision)
            is PlayerIntent.SelectTrack -> selectTrack(track = intent.track)
            is PlayerIntent.OnVolumeChange -> onVolumeChange(delta = intent.delta)
            is PlayerIntent.OnChapterButtonTap -> onChapterButtonTap(button = intent.button)
            PlayerIntent.GoToBackground -> onBackground()
            PlayerIntent.GoToForeground -> onForeground()
            is PlayerIntent.OnPipChange -> onPipChange(isInPip = intent.isInPip)
            PlayerIntent.OnClosePiP -> onClosePip()
        }
    }

    private suspend fun playMedia(media: Media) {
        val subtitlesPath = getSubtitlesUseCase(file = media.file)
        chapters = getChaptersUseCase(media = media)
        playerManager.playMedia(
            media = media,
            subtitlesPath = subtitlesPath
        )
    }

    private fun togglePlayButton() {
        playerManager.togglePlay()
    }

    private fun onFastRewind() {
        val value = currentSession?.playerRewind ?: return
        playerManager.seekRewind(value.seconds.inWholeMilliseconds)
        updateSeekOverlay(type = SeekOverlay.Type.REWIND, value = value)

    }

    private fun onFastForward() {
        val value = currentSession?.playerForward ?: return
        playerManager.seekForward(value.seconds.inWholeMilliseconds)
        updateSeekOverlay(type = SeekOverlay.Type.FORWARD, value = value)
    }

    private fun onVolumeChange(delta: Float) {
        _volume.tryEmit(playerManager.changeVolume(delta))
    }

    private fun seekTo(progress: Long, precision: SeekPrecision) {
        playerManager.seekTo(progress = progress, precision = precision)
    }

    private fun changeInterfaceVisibility() {
        _interface.update { it.copy(showInterface = !it.showInterface) }
    }

    private fun showSettingsSheet(sheet: SettingsSheet?) {
        _interface.update { it.copy(settingsSheet = sheet) }
    }

    private suspend fun updateTracks() {
        val preferredAudio = saveTrackLanguageUseCase.getAudioLanguage().toPlayerTrack(type = Type.AUDIO)
        val preferredSubtitles = if (saveTrackLanguageUseCase.subtitlesEnabled())
            saveTrackLanguageUseCase.getSubtitlesLanguage().toPlayerTrack(type = Type.SUBTITLES)
        else
            PlayerTrack.NO_SUBTITLES

        playerManager.selectTrack(track = preferredAudio)
        playerManager.selectTrack(track = preferredSubtitles)
    }

    private suspend fun selectTrack(track: PlayerTrack) {
        playerManager.selectTrack(track = track)
        saveTrackLanguageUseCase(track = track)
    }

    private suspend fun listenProgress(progress: Long) {
        _progress.update { progress }

        val currentChapter = chapters.currentAt(position = progress)
        val duration = currentSession?.duration ?: 0L

        when {
            // If it's intro or opening
            currentChapter?.type == VideoChapterType.OPENING -> {
                _overlays.update { it.copy(chapterButton = ChapterButton.Skip(chapter = currentChapter!!)) }
            }

            // If it's ending, and there's a next chapter
            currentChapter?.type == VideoChapterType.ENDING -> {

                val lastChapterIndex = chapters.maxOf { it.index }

                // If there's a chapter after
                if (currentChapter.index != lastChapterIndex) {
                    _overlays.update { it.copy(chapterButton = ChapterButton.Skip(chapter = currentChapter)) }
                } else {
                    showNextEpisode()
                }

            }

            // If the media is ended
            progress >= duration.minus(15.seconds.inWholeMilliseconds) -> {
                showNextEpisode()
            }

            else -> {
                _overlays.update { it.copy(chapterButton = null) }
            }

        }

    }

    private suspend fun showNextEpisode() {

        val media = currentSession?.media ?: return
        val cache = nextEpisodeCache
        val nextEpisode = if (cache?.first == media.mediaId) {
            cache.second
        } else {
            getNextEpisodeUseCase(media = media).also { nextEpisodeCache = media.mediaId to it }
        } ?: return

        _overlays.update { it.copy(chapterButton = ChapterButton.NextEpisode(nextEpisode = nextEpisode)) }

    }

    private suspend fun onChapterButtonTap(button: ChapterButton?) {

        when (button) {
            is ChapterButton.Skip -> {

                val endPosition = button.chapter.end
                seekTo(progress = endPosition, precision = SeekPrecision.EXACT)

            }

            is ChapterButton.NextEpisode -> {

                saveTime()
                _overlays.update { it.copy(chapterButton = null) }
                mediaId.value = button.nextEpisode.mediaId

            }

            null -> {}
        }

    }

    private suspend fun onBackTap() {

        when {
            currentSession == null -> {
                _event.send(PlayerEvent.BackToPreviousScreen)
            }
            _interface.value.showInterface -> {
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
        val media = currentSession?.media ?: return
        val progress = _progress.value

        saveProgressUseCase(media = media, progress = progress)
    }

    private fun updateSeekOverlay(type: SeekOverlay.Type, value: Int) {
        seekResetJob?.cancel()

        _overlays.update { state ->
            val current = state.seekOverlay
            val amount = if (current?.type == type) current.amount + value else value
            state.copy(seekOverlay = SeekOverlay(type = type, amount = amount))
        }

        seekResetJob = viewModelScope.launch {
            delay(1.seconds)
            _overlays.update { it.copy(seekOverlay = null) }
        }
    }

    private suspend fun onBackground() {

        if (pipIsEnabledUseCase()) return

        if (currentSession == null) return
        wasPlayingBeforeBackground = isPlaying.value

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
        _interface.update { it.copy(isInPip = isInPip) }
    }

    private suspend fun onClosePip() {
        playerManager.pause()
        saveTime()
    }

    //endregion

}