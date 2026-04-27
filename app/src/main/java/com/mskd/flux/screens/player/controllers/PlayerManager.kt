package com.mskd.flux.screens.player.controllers

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.mskd.flux.R
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.screens.player.PlayerTrack
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.uppercaseFirstLetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

class PlayerManager(private val context: Context) : Player.Listener {

    //region State

    data class State(
        val isPlaying: Boolean = false,
        val tracks: List<PlayerTrack> = emptyList(),
        val selectedAudio: PlayerTrack? = null,
        val selectedSubtitles: PlayerTrack? = null,
        val subtitles: List<Cue> = emptyList(),
        val progress: Long = 0L,
        val duration: Long = 0L,
        val showNextEpisode: Boolean = false
    )
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _player = MutableStateFlow<Player?>(null)
    val player = _player.asStateFlow()

    //endregion

    //region Variables

    private var currentSessionId: String? = null

    private var controllerFuture: ListenableFuture<MediaController>? = null

    private var currentMediaId: Long = -1L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var progressJob: Job? = null

    //endregion

    //region Lifecycle

    fun init(sessionId: String) {

        currentSessionId = sessionId

        if (_player.value != null || controllerFuture?.isDone == false) {
            return
        }

        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                controller?.apply {
                    addListener(this@PlayerManager)
                    playWhenReady = true
                }
                _player.value = controller
            } catch (e: Exception) {
                Log.e("PlayerManager", "Failed to connect", e)
                controllerFuture = null
            }
        }, MoreExecutors.directExecutor())
    }

    fun stop(sessionId: String) {

        if (currentSessionId != sessionId) return

        stopProgressMonitoring()
        _player.value?.let {
            it.stop()
            it.clearMediaItems()
        }
        currentMediaId = -1
    }

    //endregion

    //region Player events

    override fun onEvents(player: Player, events: Player.Events) {

        if (events.containsAny(
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED
            )
        ) {
            _state.update { it.copy(isPlaying = player.playWhenReady) }
            if (player.isPlaying) startProgressMonitoring() else stopProgressMonitoring()
        }

        if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)) {
            if (player.playbackState == Player.STATE_READY) {
                val duration = player.duration.coerceAtLeast(0L)

                _state.update { it.copy(duration = duration) }
            }
        }
    }

    override fun onCues(cueGroup: CueGroup) {
        _state.update { it.copy(subtitles = cueGroup.cues) }
    }

    //endregion

    //region Controls

    fun togglePlay() {
        _player.value?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun play() {
        _player.value?.play()
    }

    fun pause() {
        _player.value?.pause()
    }

    fun seekTo(progress: Long) {
        _player.value?.seekTo(progress)
    }

    fun seekRewind(value: Long) {
        _player.value?.let {
            val targetPosition = (it.currentPosition - value).coerceAtLeast(0L)
            it.seekTo(targetPosition)
        }
    }

    fun seekForward(value: Long) {
        _player.value?.let {
            val targetPosition = (it.currentPosition + value).coerceAtMost(it.duration)
            it.seekTo(targetPosition)
        }
    }

    fun changeVolume(delta: Float): Int {
        val p = _player.value ?: return 0
        val newVolume = (p.volume + delta).coerceIn(0f, 1f)
        p.volume = newVolume
        return (newVolume * 100).roundToInt()
    }

    fun playMedia(media: Media) {
        val player = _player.value ?: run {
            return
        }

        if (media.mediaId != currentMediaId) {

            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(media.title)
                .setArtist("Flux")
                .apply {
                    (media as? Episode)?.let {
                        setArtworkUri(it.imagePath.tmdbImage.toUri())
                        setSubtitle(context.getString(R.string.season_and_episode, it.season, it.number))
                    }
                }
                .build()

            val mediaItem = MediaItem.Builder()
                .setMediaMetadata(mediaMetadata)
                .setUri(media.file.path.toUri())
                .build()

            player.stop()
            player.clearMediaItems()

            currentMediaId = media.mediaId
            player.setMediaItem(mediaItem, media.currentTime)
            player.prepare()
        }

        player.play()

    }

    //endregion

    //region Tracks

    override fun onTracksChanged(tracks: Tracks) {
        val defaultLabel = context.getString(R.string.track)

        var selectedAudio: PlayerTrack? = null
        var selectedSubtitles: PlayerTrack? = null

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
            it.copy(
                tracks = tracks,
                selectedAudio = selectedAudio ?: it.selectedAudio,
                selectedSubtitles = selectedSubtitles ?: it.selectedSubtitles
            )
        }

    }

    fun selectTrack(track: PlayerTrack) {
        val player = _player.value ?: return
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
                PlayerTrack.Type.AUDIO -> _state.update { it.copy(selectedAudio = t) }
                PlayerTrack.Type.SUBTITLES -> _state.update { it.copy(selectedSubtitles = t) }
            }
        }
    }

    private fun TrackSelectionParameters.Builder.applyAudioTrack(track: PlayerTrack, currentTracks: Tracks) : PlayerTrack? {
        clearOverridesOfType(C.TRACK_TYPE_AUDIO)

        if (track.id != null) {

            val result = applyTrackOverride(trackId = track.id, groups = currentTracks.groups)
            if (result) return track

        } else {

            val playerTrack = _state.value.tracks.filter { it.type == PlayerTrack.Type.SUBTITLES }.firstOrNull { it.language == track.language }
            playerTrack?.language?.let {
                setPreferredAudioLanguage(it)
                return playerTrack
            }

        }

        return null

    }

    private fun TrackSelectionParameters.Builder.applySubtitlesTrack(track: PlayerTrack, currentTracks: Tracks) : PlayerTrack? {

        clearOverridesOfType(C.TRACK_TYPE_TEXT)

        if (track.language == null) { // If no subtitle

            setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)

        } else {

            setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)

            // Select track from player tracks or user preferences
            if (track.id != null) {

                val result = applyTrackOverride(trackId = track.id, groups = currentTracks.groups)
                if (result) return track

            } else {

                val playerTrack = _state.value.tracks.filter { it.type == PlayerTrack.Type.SUBTITLES }.firstOrNull { it.language == track.language }
                playerTrack?.language?.let {
                    setPreferredTextLanguage(it)
                    return playerTrack
                }

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
            Log.e("PlayerStateHolder", "Fail to apply track", e)
            return false
        }

    }

    private fun buildLabel(format: Format): String? {
        return format.language?.let { language ->
            val locale = Locale.forLanguageTag(language)
            locale.getDisplayName(locale).uppercaseFirstLetter()
        }
    }

    //endregion

    //region Progress monitoring

    private fun startProgressMonitoring() {
        stopProgressMonitoring()
        progressJob = scope.launch {
            while (isActive) {
                _player.value?.let { currentPlayer ->
                    if (currentPlayer.isPlaying && currentPlayer.duration > 0) {

                        val progressPercentage = currentPlayer.currentPosition.toFloat() / currentPlayer.duration.toFloat()

                        _state.update {
                            it.copy(
                                progress = currentPlayer.currentPosition,
                                showNextEpisode = progressPercentage >= Constants.PLAYER.PROGRESS_THRESHOLD
                            )
                        }

                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopProgressMonitoring() {
        progressJob?.cancel()
        progressJob = null
    }

    //endregion

}