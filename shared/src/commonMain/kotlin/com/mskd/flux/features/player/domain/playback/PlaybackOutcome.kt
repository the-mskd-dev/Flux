package com.mskd.flux.features.player.domain.playback

import com.mskd.flux.core.model.artwork.Media

/** What a page must do once it asked to play a Media. */
sealed interface PlaybackOutcome {
    /** The page opens the Internal player on [media]. */
    data class OpenInternalPlayer(val media: Media) : PlaybackOutcome

    /** The External player was started: nothing to do for the page. */
    data object HandedToExternalPlayer : PlaybackOutcome

    /** The Media is Unavailable: no player is started. */
    data object Unavailable : PlaybackOutcome
}
