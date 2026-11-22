package com.mskd.flux.features.player.domain.playback

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.progress.domain.ProgressTracker
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.report.Trace
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

/**
 * Playback launch: from "play this Media" to "the Progress reported by the External player is remembered".
 * Decides between the Internal and External player, falls back to the Internal player when the External one
 * cannot start, and records the Progress it reports on the Media that was launched. The pending launch is persisted,
 * so the result is still recorded when Flux was killed while the External player was open.
 */
class PlaybackLaunch(
    private val settings: SettingsDataStore,
    private val externalPlayer: ExternalPlayerAdapter,
    private val progressTracker: ProgressTracker,
    private val pendingLaunchStore: PendingLaunchStore,
    private val database: DatabaseRepository,
) {

    private companion object {
        const val TAG = "PlaybackLaunch"
    }

    private val internalPlayerChannel = Channel<Media>(Channel.BUFFERED)

    /**
     * Media to open in the Internal player because the External player failed after it had started.
     * Each request is delivered once, to whoever collects first (a page may be gone by then).
     */
    val internalPlayerRequests: Flow<Media> = internalPlayerChannel.receiveAsFlow()

    suspend fun play(media: Media, forceInternal: Boolean): PlaybackOutcome {
        pendingLaunchStore.clear() // a stale launch must never capture a later result
        if (!media.isAvailable) return PlaybackOutcome.Unavailable
        if (forceInternal || !settings.externalPlayerIsEnabled()) return PlaybackOutcome.OpenInternalPlayer(media)

        // Set before the adapter is called: a result delivered immediately must not be dropped.
        pendingLaunchStore.set(PendingLaunch(artworkId = media.artworkId, mediaId = media.mediaId))
        val failure = when (val result = externalPlayer.launch(media)) {
            ExternalLaunchResult.Launched -> return PlaybackOutcome.HandedToExternalPlayer
            ExternalLaunchResult.NoPlayerInstalled -> "No player found" to null
            ExternalLaunchResult.PermissionDenied -> "Permission denied" to null
            is ExternalLaunchResult.Failed -> "Fail to launch external player" to result.cause
        }
        pendingLaunchStore.clear()
        Trace.error(TAG, failure.first, failure.second)
        return PlaybackOutcome.OpenInternalPlayer(media)
    }

    /** The External player reported [position] (null when it reported none). Recorded at most once per launch. */
    suspend fun onExternalResult(position: Long?) = withContext(NonCancellable) {
        // Not cancellable: the pending launch is consumed first, so a cancellation (Activity destroyed) before the
        // save would lose the Progress for good.
        val media = takePending() ?: return@withContext
        if (position == null) return@withContext
        progressTracker.recordProgress(media = media, position = position)
    }

    /**
     * The External player failed after it started (cancelled with an error). Falls back to the Internal player on
     * the launched Media, without retrying the External one. A failure with no pending launch does nothing.
     */
    suspend fun onExternalFailure(cause: Throwable? = null) = withContext(NonCancellable) {
        val media = takePending() ?: return@withContext
        Trace.error(TAG, "External player failed after launch", cause)
        internalPlayerChannel.trySend(media)
        Unit
    }

    /**
     * The launched Media, found again in the database (the process may have been killed since the launch), and
     * forgotten. Null when nothing is pending or when the Media no longer exists.
     */
    private suspend fun takePending(): Media? {
        val launch = pendingLaunchStore.take() ?: return null
        return database.getEpisodes(launch.artworkId).find { it.mediaId == launch.mediaId }
            ?: database.getMovie(launch.artworkId)?.takeIf { it.mediaId == launch.mediaId }
    }

}
