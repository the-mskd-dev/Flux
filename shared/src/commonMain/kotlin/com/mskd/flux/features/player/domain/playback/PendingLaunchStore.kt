package com.mskd.flux.features.player.domain.playback

/** Identity of the Media handed to the External player, enough to find it again after Flux was killed. */
data class PendingLaunch(val artworkId: Long, val mediaId: Long)

/** Persisted pending launch: survives process death while the External player is open. */
interface PendingLaunchStore {

    suspend fun set(launch: PendingLaunch)

    suspend fun clear()

    /** Returns the pending launch and clears it in one step, so that it is handed out at most once. */
    suspend fun take(): PendingLaunch?

}
