package com.mskd.flux.platform

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.services.PlayerService
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.uppercaseFirstLetter
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.season_and_episode
import flux.shared.generated.resources.track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

class AndroidPlayerManager(private val context: Context) : Player.Listener, PlayerManager<Player> {

    private companion object {
        const val TAG = "AndroidPlayerManager"
    }

    //region State

    private val _state = MutableStateFlow<PlayerManager.State<Player>>(PlayerManager.State.Idle)
    override val flow: Flow<PlayerManager.State<Player>> = _state.asStateFlow()

    private val _subtitles = MutableStateFlow<List<String?>>(emptyList())
    override val subtitles: Flow<List<String?>> = _subtitles.asStateFlow()

    private val _progress = MutableStateFlow(PlayerManager.Progress())
    override val progress: Flow<PlayerManager.Progress> = _progress.asStateFlow()

    //endregion

    //region Variables

    private var controllerFuture: ListenableFuture<MediaController>? = null

    private var currentMediaId: Long = -1L
    private var currentSessionId: String? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null

    //endregion

    //region Public Methods

    override fun connect(sessionId: String) {

        currentSessionId = sessionId

        if (_state.value is PlayerManager.State.Connecting || _state.value is PlayerManager.State.Ready) {
            return
        }

        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get() ?: run {
                    _state.value = PlayerManager.State.Error()
                    return@addListener
                }

                controller.addListener(this@AndroidPlayerManager)
                _state.value = PlayerManager.State.Ready(player = controller)
            } catch (e: Exception) {
                Trace.error(tag = TAG, message = "Failed to connect", throwable = e)
                _state.value = PlayerManager.State.Error()
            }
        }, MoreExecutors.directExecutor())

    }

    override fun disconnect(sessionId: String) {

        if (currentSessionId != sessionId) return

        stopProgressMonitoring()

        val currentState = _state.value
        if (currentState is PlayerManager.State.Ready) {
            currentState.player.removeListener(this)
            currentState.player.stop()
            currentState.player.clearMediaItems()
        }

        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        currentMediaId = -1L
        _state.value = PlayerManager.State.Idle

    }

    override fun togglePlay() {
        (_state.value as? PlayerManager.State.Ready)?.let {
            if (it.isPlaying) it.player.pause() else it.player.play()
        }
    }

    override fun play() {
        (_state.value as? PlayerManager.State.Ready)?.player?.play()
    }

    override fun pause() {
        (_state.value as? PlayerManager.State.Ready)?.player?.pause()
    }

    override fun seekTo(progress: Long) {
        (_state.value as? PlayerManager.State.Ready)?.player?.seekTo(progress)
    }

    override fun seekRewind(value: Long) {
        (_state.value as? PlayerManager.State.Ready)?.player?.let {
            val targetPosition = (it.currentPosition - value).coerceAtLeast(0L)
            it.seekTo(targetPosition)
        }
    }

    override fun seekForward(value: Long) {
        (_state.value as? PlayerManager.State.Ready)?.player?.let {
            val targetPosition = (it.currentPosition + value).coerceAtMost(it.duration)
            it.seekTo(targetPosition)
        }
    }

    override fun changeVolume(delta: Float): Int {
        val player = (_state.value as? PlayerManager.State.Ready)?.player ?: return 0
        val newVolume = (player.volume + delta).coerceIn(0f, 1f)
        player.volume = newVolume
        return (newVolume * 100).roundToInt()
    }

    override suspend fun playMedia(media: Media, subtitlesPath: String?) {
        val player = (_state.value as? PlayerManager.State.Ready)?.player ?: return

        if (media.mediaId != currentMediaId) {

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(media.title)
                .setArtist("Flux")
                .apply {
                    (media as? Episode)?.let {
                        setArtworkUri(it.imagePath.tmdbImage.toUri())
                        setSubtitle(getString(Res.string.season_and_episode, it.season, it.number))
                    }
                }
                .build()

            val mediaItemBuilder = MediaItem.Builder()
                .setMediaMetadata(mediaMetadata)
                .setUri(media.file.path.toUri())

            // Add local subtitles
            createSubtitlesFrom(subtitlesUri = subtitlesPath?.toUri())?.let { subtitle ->
                mediaItemBuilder.setSubtitleConfigurations(listOf(subtitle))
            }

            val mediaItem = mediaItemBuilder.build()

            player.stop()
            player.clearMediaItems()

            currentMediaId = media.mediaId
            player.setMediaItem(mediaItem, media.currentTime)
            player.prepare()
        }

        player.play()

    }

    override fun selectTrack(track: PlayerTrack) {
        val current = _state.value as? PlayerManager.State.Ready ?: return
        val player = current.player
        val currentTracks = player.currentTracks
        var selectedTrack: PlayerTrack? = null

        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .apply {
                selectedTrack = when (track.type) {
                    PlayerTrack.Type.AUDIO -> applyAudioTrack(track, currentTracks)
                    PlayerTrack.Type.SUBTITLES -> applySubtitlesTrack(track, currentTracks)
                }
            }
            .build()

        selectedTrack?.let { t ->
            when (t.type) {
                PlayerTrack.Type.AUDIO -> _state.update { current.copy(selectedAudio = t) }
                PlayerTrack.Type.SUBTITLES -> _state.update { current.copy(selectedSubtitles = t) }
            }
        }
    }

    //endregion

    //region Player events

    override fun onEvents(player: Player, events: Player.Events) {
        val currentState = _state.value as? PlayerManager.State.Ready ?: return

        if (events.containsAny(
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED
            )
        ) {
            _state.update { currentState.copy(isPlaying = player.playWhenReady) }
            if (player.isPlaying) startProgressMonitoring() else stopProgressMonitoring()
        }

        if (
            events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
            && player.playbackState == Player.STATE_READY
        ) {
            val duration = player.duration.coerceAtLeast(0L)
            _state.update { currentState.copy(duration = duration) }
        }

    }

    override fun onCues(cueGroup: CueGroup) {
        _subtitles.update { cueGroup.cues.map { it.text?.toString() } }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        _state.update {
            PlayerManager.State.Error(
                code = error.errorCode,
                name = error.errorCodeName
            )
        }
    }

    //endregion

    //region Private Methods

    private fun createSubtitlesFrom(subtitlesUri: Uri?) : MediaItem.SubtitleConfiguration? {

        subtitlesUri ?: return null

        val mimeType = when (subtitlesUri.toString().substringAfterLast(".").lowercase()) {
            "vtt" -> MimeTypes.TEXT_VTT
            "ass", "ssa" -> MimeTypes.TEXT_SSA
            else -> MimeTypes.APPLICATION_SUBRIP
        }

        val subtitle = MediaItem.SubtitleConfiguration.Builder(subtitlesUri)
            .setMimeType(mimeType)
            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
            .build()

        return subtitle

    }

    override fun onTracksChanged(tracks: Tracks) {
        val current = _state.value as? PlayerManager.State.Ready ?: return

        val defaultLabel = runBlocking { getString(Res.string.track) }

        var selectedAudio: PlayerTrack? = current.selectedAudio
        var selectedSubtitles: PlayerTrack? = current.selectedSubtitles

        val tracks = tracks.groups
            .filter { it.type == C.TRACK_TYPE_AUDIO || it.type == C.TRACK_TYPE_TEXT }
            .flatMap { group ->
                (0 until group.length).map { index ->
                    val format = group.getTrackFormat(index)
                    val id = "${tracks.groups.indexOf(group)}:$index:${format.id}"
                    val isSelected = group.isTrackSelected(index)

                    val playerTrack = PlayerTrack(
                        id = id,
                        label = format.label ?: buildLabel(format) ?: "$defaultLabel #${index + 1}",
                        language = format.language,
                        type = if (group.type == C.TRACK_TYPE_AUDIO) PlayerTrack.Type.AUDIO else PlayerTrack.Type.SUBTITLES
                    )

                    if (isSelected) {
                        when (playerTrack.type) {
                            PlayerTrack.Type.AUDIO -> selectedAudio = playerTrack
                            PlayerTrack.Type.SUBTITLES -> selectedSubtitles = playerTrack
                        }
                    }

                    playerTrack
                }
            }

        _state.update {
            current.copy(
                tracks = tracks,
                selectedAudio = selectedAudio,
                selectedSubtitles = selectedSubtitles
            )
        }

    }

    private fun TrackSelectionParameters.Builder.applyAudioTrack(track: PlayerTrack, currentTracks: Tracks) : PlayerTrack? {
        val current = _state.value as? PlayerManager.State.Ready ?: return null
        clearOverridesOfType(C.TRACK_TYPE_AUDIO)

        track.id?.let { trackId ->
            val result = applyTrackOverride(trackId = trackId, groups = currentTracks.groups)
            if (result) return track
        }

        val playerTrack = current.tracks.filter { it.type == PlayerTrack.Type.AUDIO }.firstOrNull { it.language == track.language }
        playerTrack?.language?.let {
            setPreferredAudioLanguage(it)
            return playerTrack
        }

        return null

    }

    private fun TrackSelectionParameters.Builder.applySubtitlesTrack(track: PlayerTrack, currentTracks: Tracks) : PlayerTrack? {
        val current = _state.value as? PlayerManager.State.Ready ?: return null
        clearOverridesOfType(C.TRACK_TYPE_TEXT)

        if (track == PlayerTrack.NO_SUBTITLES) { // If no subtitle

            setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            return track

        } else {

            setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)

            // Select track from player tracks or user preferences
            track.id?.let { trackId ->
                val result = applyTrackOverride(trackId = trackId, groups = currentTracks.groups)
                if (result) return track
            }

            val playerTrack = current.tracks.filter { it.type == PlayerTrack.Type.SUBTITLES }.firstOrNull { it.language == track.language }
            playerTrack?.language?.let {
                setPreferredTextLanguage(it)
                return playerTrack
            }

        }

        return null

    }

    private fun TrackSelectionParameters.Builder.applyTrackOverride(
        trackId: String,
        groups: List<Tracks.Group>
    ) : Boolean {

        try {
            val parts = trackId.split(":")
            val groupIndex = parts[0].toInt()
            val trackIndex = parts[1].toInt()

            if (groupIndex < groups.size) {
                val group = groups[groupIndex].mediaTrackGroup
                addOverride(TrackSelectionOverride(group, trackIndex))
                return true
            }

            return false

        } catch (e: Exception) {
            Trace.error(TAG, "Fail to apply track", e)
            return false
        }

    }

    private fun buildLabel(format: Format): String? {
        return format.language?.let { language ->
            val locale = Locale.forLanguageTag(language)
            locale.getDisplayName(locale).uppercaseFirstLetter()
        }
    }

    private fun startProgressMonitoring() {
        stopProgressMonitoring()
        progressJob = scope.launch {
            while (isActive) {

                (_state.value as? PlayerManager.State.Ready)?.let { ready ->

                    val player = ready.player

                    if (player.isPlaying && player.duration > 0) {

                        val progressPercentage = player.currentPosition.toFloat() / player.duration.toFloat()
                        val showNextEpisode = progressPercentage >= Constants.PLAYER.PROGRESS_THRESHOLD

                        _progress.update {
                            it.copy(
                                progress = player.currentPosition,
                                showNextEpisode = showNextEpisode
                            )
                        }

                    }

                }

                delay(1000.milliseconds)
            }
        }
    }

    private fun stopProgressMonitoring() {
        progressJob?.cancel()
        progressJob = null
    }

    //endregion

}
