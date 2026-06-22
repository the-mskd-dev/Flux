package com.mskd.flux.screen.player

import androidx.core.net.toUri
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.player.PlayerTrack
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.tmdbImage
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.season_and_episode
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.getString
import kotlin.math.roundToInt

interface PlayerManager {

    sealed class State {
        data object Idle : State()
        data object Connecting : State()
        data class Ready(
            val playerWrapper: PlayerWrapper,
            val isPlaying: Boolean = false,
            val tracks: List<PlayerTrack> = emptyList(),
            val selectedAudio: PlayerTrack? = null,
            val selectedSubtitles: PlayerTrack? = null,
            val subtitles: PlayerSubtitles,
            val progress: Long = 0L,
            val duration: Long = 0L,
            val showNextEpisode: Boolean = false
        ) : State()
        data object Error : State()
    }

    val flow: Flow<State>

    fun connect(sessionId: String)

    fun disconnect(sessionId: String)

    fun togglePlay()

    fun play()

    fun pause()

    fun seekTo(progress: Long)

    fun seekRewind(value: Long)

    fun seekForward(value: Long)

    fun changeVolume(delta: Float): Int

    suspend fun playMedia(media: Media, subtitlesPath: String?)

    fun selectTrack(track: PlayerTrack)

}

interface PlayerWrapper

interface PlayerSubtitles