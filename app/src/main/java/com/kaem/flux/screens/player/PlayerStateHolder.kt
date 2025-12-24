package com.kaem.flux.screens.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.extractor.text.Subtitle
import androidx.room.Index
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.utils.extensions.findActivity
import com.kaem.flux.utils.extensions.forceScreenOn
import com.kaem.flux.utils.extensions.hideSystemBars
import com.kaem.flux.utils.extensions.setAppInLandscape
import com.kaem.flux.utils.extensions.setAppOrientation
import com.kaem.flux.utils.extensions.showSystemBars
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

@UnstableApi
@Stable
class PlayerStateHolder(context: Context) : Player.Listener {

    //region States

    private val _subtitles = MutableStateFlow<List<Cue>>(emptyList())
    val subtitles: StateFlow<List<Cue>> = _subtitles.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _tracks = MutableStateFlow<List<PlayerTrack>>(emptyList())
    val tracks = _tracks.asStateFlow()

    //endregion

    //region Player

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

    private var currentMediaId: Long = -1L

    override fun onCues(cueGroup: androidx.media3.common.text.CueGroup) {
        _subtitles.value = cueGroup.cues
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_IS_PLAYING_CHANGED
            )
        ) {
            _isPlaying.value = player.playWhenReady
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)

        val audiosTracks = tracks.groups
            .filter { it.type == C.TRACK_TYPE_AUDIO }
            .flatMap { group ->
                (0 until group.length).map { index ->
                    val format = group.getTrackFormat(index)
                    val id = "${tracks.groups.indexOf(group)}:$index:${format.id}"
                    PlayerTrack(
                        id = id,
                        name = format.label ?: buildLabel(format = format) ?: "Audio #${index + 1}",
                        language = format.language,
                        type = PlayerTrack.Type.AUDIO
                    )
                }
            }

        val subtitlesTracks = tracks.groups
            .filter { it.type == C.TRACK_TYPE_TEXT }
            .flatMap { group ->
                (0 until group.length).map { index ->
                    val format = group.getTrackFormat(index)
                    val id = "${tracks.groups.indexOf(group)}:$index:${format.id}"
                    PlayerTrack(
                        id = id,
                        name = format.label ?: buildLabel(format = format) ?: "Subtitles #${index + 1}",
                        language = format.language,
                        type = PlayerTrack.Type.SUBTITLES
                    )
                }
            }

        _tracks.value = audiosTracks + subtitlesTracks

    }

    private fun buildLabel(format: Format): String? {
        return format.language?.let { language ->
            val locale = Locale.forLanguageTag(language)
            locale.getDisplayName(locale)
        }
    }

    fun playMedia(media: Media?) {

        if (media != null && media.mediaId != currentMediaId) {
            player.setMediaItem(MediaItem.fromUri(media.file.path.toUri()))
            player.seekTo(media.currentTime)
            player.prepare()
        }

    }

    fun selectTrack(type: PlayerTrack.Type, language: String?) {

        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .apply {
                when (type) {
                    PlayerTrack.Type.AUDIO -> {
                        language?.let { setPreferredAudioLanguage(it) }
                    }
                    PlayerTrack.Type.SUBTITLES -> {

                        setTrackTypeDisabled(C.TRACK_TYPE_TEXT, language == null)
                        if (language != null) setPreferredTextLanguage(language)
                    }
                }
            }
            .build()

    }

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

    fun releasePlayer() {
        player.release()
    }

    //endregion

    //region Screen

    private val activity = context.findActivity()

    fun setLandscape() {
        activity?.setAppInLandscape()
        activity?.forceScreenOn(true)
    }

    fun resetOrientation(originalOrientation: Int) {
        activity?.setAppOrientation(originalOrientation)
        activity?.forceScreenOn(false)
    }

    fun updateSystemBars(show: Boolean) {
        if (show) activity?.showSystemBars() else activity?.hideSystemBars()
    }

    //endregion
}

@OptIn(UnstableApi::class)
@Composable
fun rememberPlayerStateHolder(
    context: Context = LocalContext.current
): PlayerStateHolder {
    val holder = remember(context) {
        PlayerStateHolder(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            holder.releasePlayer()
        }
    }

    return holder
}