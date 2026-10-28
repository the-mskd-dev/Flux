package com.mskd.flux.features.player.domain.manager

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.player.PlayerTrack
import kotlinx.coroutines.flow.Flow

interface PlayerManager<out T> {

    sealed class State<out T> {
        data object Idle : State<Nothing>()
        data object Connecting : State<Nothing>()
        data class Ready<T>(
            val player: T,
            val isPlaying: Boolean = false,
            val tracks: List<PlayerTrack> = emptyList(),
            val selectedAudio: PlayerTrack? = null,
            val selectedSubtitles: PlayerTrack? = null,
            val duration: Long = 0L,
        ) : State<T>()
        data class Error(val code: Int? = null, val name: String? = null) : State<Nothing>()
    }

    val flow: Flow<State<T>>

    val subtitles: Flow<List<String?>>

    val progress: Flow<Long>

    fun connect(sessionId: String)

    fun disconnect(sessionId: String)

    fun togglePlay()

    fun play()

    fun pause()

    fun seekTo(progress: Long, precision: SeekPrecision = SeekPrecision.EXACT)

    fun seekRewind(value: Long)

    fun seekForward(value: Long)

    fun changeVolume(delta: Float): Int

    suspend fun playMedia(media: Media, subtitlesPath: String?)

    fun selectTrack(track: PlayerTrack)

}
/** FAST snaps to the closest keyframe (cheap, for scrubbing); EXACT lands on the requested position. */
enum class SeekPrecision { FAST, EXACT }
