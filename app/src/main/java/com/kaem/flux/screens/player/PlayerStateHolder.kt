package com.kaem.flux.screens.player

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.utils.extensions.forceScreenOn
import com.kaem.flux.utils.extensions.hideSystemBars
import com.kaem.flux.utils.extensions.setAppInLandscape
import com.kaem.flux.utils.extensions.setAppOrientation
import com.kaem.flux.utils.extensions.showSystemBars
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
class PlayerStateHolder(private val activity: ComponentActivity) : Player.Listener {

    //region States

    private val _subtitlesState = MutableStateFlow<List<Cue>>(emptyList())
    val subtitlesState: StateFlow<List<Cue>> = _subtitlesState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    //endregion

    //region Player

    val player : Player = ExoPlayer.Builder(activity)
        .setRenderersFactory(
            DefaultRenderersFactory(activity)
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
        _subtitlesState.value = cueGroup.cues
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

    fun playMedia(media: Media?) {

        if (media != null && media.mediaId != currentMediaId) {
            player.setMediaItem(MediaItem.fromUri(media.file.path.toUri()))
            player.seekTo(media.currentTime)
            player.prepare()
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

    fun setLandscape() {
        activity.setAppInLandscape()
        activity.forceScreenOn(true)
    }

    fun resetOrientation(originalOrientation: Int) {
        activity.setAppOrientation(originalOrientation)
        activity.forceScreenOn(false)
    }

    fun updateSystemBars(show: Boolean) {
        if (show) activity.showSystemBars() else activity.hideSystemBars()
    }

    //endregion
}

@Composable
fun rememberPlayerStateHolder(
    activity: ComponentActivity = LocalActivity.current as ComponentActivity
): PlayerStateHolder {
    val holder = remember(activity) {
        PlayerStateHolder(activity)
    }

    DisposableEffect(Unit) {
        onDispose {
            holder.releasePlayer()
        }
    }

    return holder
}