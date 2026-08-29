package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore

class ResolvePlaybackActionUseCase(
    private val settings: SettingsDataStore
) {
    suspend operator fun invoke(media: Media, forceInternal: Boolean): PlaybackAction {
        if (!media.isAvailable) return PlaybackAction.Unavailable

        return if (settings.externalPlayerIsEnabled() && !forceInternal)
            PlaybackAction.OpenExternalPlayer(media)
        else
            PlaybackAction.OpenInternalPlayer(media.mediaId)
    }
}