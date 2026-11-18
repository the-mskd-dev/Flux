package com.mskd.flux.features.player.domain.playback

import com.mskd.flux.core.model.artwork.Media

/** Platform seam: starts the External player on a Media. */
interface ExternalPlayerAdapter {
    fun launch(media: Media): ExternalLaunchResult
}

sealed interface ExternalLaunchResult {
    data object Launched : ExternalLaunchResult
    data object NoPlayerInstalled : ExternalLaunchResult
    data object PermissionDenied : ExternalLaunchResult
    data class Failed(val cause: Throwable? = null) : ExternalLaunchResult
}
