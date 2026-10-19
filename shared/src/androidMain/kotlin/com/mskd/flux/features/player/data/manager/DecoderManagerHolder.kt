package com.mskd.flux.features.player.data.manager

import androidx.media3.common.util.UnstableApi
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.DecoderManager

/**
 * Bridges the [DecoderManager] of the running `PlayerService` to [AndroidPlayerManager].
 *
 * The manager is owned by the player service scope because NextRenderersFactory can only wrap
 * one renderer set: it must die with the player it was created for. [AndroidPlayerManager] is
 * created outside of that scope (it is resolved from the root Koin context by the ViewModel)
 * but still needs the live manager to switch decoders at runtime.
 *
 * The service publishes its manager on create and clears it on destroy, so consumers never
 * hold a manager that outlived its player.
 */
@UnstableApi
class DecoderManagerHolder {

    /** The manager attached to the player of the running service, or null when none is alive. */
    @Volatile
    var decoderManager: DecoderManager? = null
}
