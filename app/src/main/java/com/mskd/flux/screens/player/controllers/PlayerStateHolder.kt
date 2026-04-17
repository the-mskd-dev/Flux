package com.mskd.flux.screens.player.controllers

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.mskd.flux.R
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.screens.player.PlayerTrack
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.extensions.uppercaseFirstLetter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale


@OptIn(UnstableApi::class)
@Stable
class PlayerStateHolder(
    private val context: Context,
    private val scope: CoroutineScope
) : Player.Listener {

    //region States

    private val _subtitles = MutableStateFlow<List<Cue>>(emptyList())
    val subtitles: StateFlow<List<Cue>> = _subtitles.asStateFlow()

    private val _tracks = MutableStateFlow<List<PlayerTrack>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _event = Channel<Event>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    sealed class Event {
        data class IsPlaying(val isPlaying: Boolean) : Event()
        data class SelectedTrack(val track: PlayerTrack) : Event()
        data class ShowNext(val show: Boolean) : Event()
    }

    //endregion

    //region Variables

    private var currentMediaId: Long = -1L

    private var progressJob: Job? = null

    val player : Player = ExoPlayer.Builder(context)
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
            addListener(this@PlayerStateHolder)
        }

    //endregion

    //region Listener methods

    override fun onCues(cueGroup: CueGroup) {
        _subtitles.value = cueGroup.cues
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED
            )
        ) {
            scope.launch {
                _event.send(Event.IsPlaying(isPlaying = player.playWhenReady))
            }

            if (player.isPlaying) startProgressMonitoring() else stopProgressMonitoring()

        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)

        val defaultLabel  by lazy { context.getString(R.string.track) }

        _tracks.value = tracks.groups
            .filter { it.type == C.TRACK_TYPE_AUDIO || it.type == C.TRACK_TYPE_TEXT }
            .flatMap { group ->
                (0 until group.length).map { index ->
                    val format = group.getTrackFormat(index)
                    val isSelected = group.isTrackSelected(index)
                    val id = "${tracks.groups.indexOf(group)}:$index:${format.id}"

                    val playerTrack = PlayerTrack(
                        id = id,
                        label = format.label ?: buildLabel(format = format)
                        ?: "$defaultLabel #${index + 1}",
                        language = format.language,
                        type = if (group.type == C.TRACK_TYPE_AUDIO) PlayerTrack.Type.AUDIO else PlayerTrack.Type.SUBTITLES
                    )

                    if (isSelected) {
                        scope.launch {
                            _event.send(Event.SelectedTrack(track = playerTrack))
                        }
                    }

                    playerTrack
                }
            }

    }

    //endregion

    //region Private methods

    private fun buildLabel(format: Format): String? {
        return format.language?.let { language ->
            val locale = Locale.forLanguageTag(language)
            locale.getDisplayName(locale).uppercaseFirstLetter()
        }
    }

    private fun TrackSelectionParameters.Builder.applyAudioTrack(track: PlayerTrack, currentTracks: Tracks) : PlayerTrack? {
        clearOverridesOfType(C.TRACK_TYPE_AUDIO)

        if (track.id != null) {

            val result = applyTrackOverride(trackId = track.id, groups = currentTracks.groups)
            if (result) return track

        } else {

            val playerTrack = _tracks.value.filter { it.type == PlayerTrack.Type.SUBTITLES }.firstOrNull { it.language == track.language }
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

                val playerTrack = _tracks.value.filter { it.type == PlayerTrack.Type.SUBTITLES }.firstOrNull { it.language == track.language }
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

    private fun startProgressMonitoring() {
        stopProgressMonitoring()
        progressJob = scope.launch {

            var lastNextEpisodeEvent: Boolean? = null

            while (isActive) {

                if (player.duration > 0) {

                    val percentage = player.currentPosition.toFloat() / player.duration.toFloat()
                    val showNext = percentage >= Constants.PLAYER.PROGRESS_THRESHOLD

                    if (lastNextEpisodeEvent != showNext) {
                        _event.send(Event.ShowNext(show = showNext))
                        lastNextEpisodeEvent = showNext
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

    //region Public methods

    fun togglePlayButton() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun onFastRewind(value: Long) {
        player.seekTo(player.currentPosition - value)
    }

    fun onFastForward(value: Long) {
        player.seekTo(player.currentPosition + value)
    }

    fun updateProgress(progress: Long) {
        player.seekTo(progress)
    }

    fun playMedia(media: Media?) {

        if (media != null && media.mediaId != currentMediaId) {
            player.setMediaItem(MediaItem.fromUri(media.file.path.toUri()))
            player.seekTo(media.currentTime)
            player.prepare()
            currentMediaId = media.mediaId
            startProgressMonitoring()
        }

    }

    fun releasePlayer() {
        player.release()
    }

    suspend fun selectTrack(track: PlayerTrack) {

        val currentTracks = player.currentTracks
        var trackResult: PlayerTrack? = null

        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .apply {
                trackResult = when (track.type) {
                    PlayerTrack.Type.AUDIO -> applyAudioTrack(track = track, currentTracks = currentTracks)
                    PlayerTrack.Type.SUBTITLES -> applySubtitlesTrack(track = track, currentTracks = currentTracks)
                }
            }
            .build()

        trackResult?.let {
            _event.send(Event.SelectedTrack(track = it))
            Log.i("PlayerStateHolder", "Selected track: $it")
        }

    }

    //endregion

}

@OptIn(UnstableApi::class)
@Composable
fun rememberPlayerStateHolder(
    context: Context = LocalContext.current,
    scope: CoroutineScope = rememberCoroutineScope()
): PlayerStateHolder {

    val holder = remember(context) {
        PlayerStateHolder(
            context = context,
            scope = scope
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            holder.releasePlayer()
        }
    }

    return holder
}