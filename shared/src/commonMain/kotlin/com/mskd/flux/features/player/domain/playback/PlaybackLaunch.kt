package com.mskd.flux.features.player.domain.playback

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * Playback launch: from "play this Media" to "the Progress reported by the External player is remembered".
 * Decides between the Internal and External player, falls back to the Internal player when the External one
 * cannot start, and records the Progress it reports on the Media that was launched.
 */
class PlaybackLaunch(
    private val settings: SettingsDataStore,
    private val externalPlayer: ExternalPlayerAdapter,
    private val saveProgress: SaveProgressUseCase,
) {

    private companion object {
        const val TAG = "PlaybackLaunch"
    }

    /** The Media handed to the External player, waiting for its result. Lost on process death. */
    private val pending = MutableStateFlow<Media?>(null)

    suspend fun play(media: Media, forceInternal: Boolean): PlaybackOutcome {
        if (!media.isAvailable) return PlaybackOutcome.Unavailable
        if (forceInternal || !settings.externalPlayerIsEnabled()) return PlaybackOutcome.OpenInternalPlayer(media)

        return when (val result = externalPlayer.launch(media)) {
            ExternalLaunchResult.Launched -> {
                pending.value = media
                PlaybackOutcome.HandedToExternalPlayer
            }
            else -> {
                val (kind, cause) = when (result) {
                    ExternalLaunchResult.NoPlayerInstalled -> "No player found" to null
                    ExternalLaunchResult.PermissionDenied -> "Permission denied" to null
                    is ExternalLaunchResult.Failed -> "Fail to launch external player" to result.cause
                    ExternalLaunchResult.Launched -> error("unreachable")
                }
                Trace.error(TAG, kind, cause)
                PlaybackOutcome.OpenInternalPlayer(media)
            }
        }
    }

    /** The External player reported [position] (null when it reported none). Recorded at most once per launch. */
    suspend fun onExternalResult(position: Long?) {
        val media = pending.getAndUpdate { null } ?: return
        if (position == null) return
        saveProgress(media = media, progress = position)
    }

}
