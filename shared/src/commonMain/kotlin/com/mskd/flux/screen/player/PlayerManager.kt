package com.mskd.flux.screen.player

import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.player.PlayerTrack
import kotlinx.coroutines.flow.Flow

interface PlayerManager<out T, out R> {

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
        data object Error : State<Nothing>()
    }

    data class Progress(
        val progress: Long = 0L,
        val showNextEpisode: Boolean = false
    )

    val flow: Flow<State<T>>

    val subtitles: Flow<R>

    val progress: Flow<Progress>

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
