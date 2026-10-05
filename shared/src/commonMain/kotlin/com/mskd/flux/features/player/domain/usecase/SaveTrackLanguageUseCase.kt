package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first

class SaveTrackLanguageUseCase(
    private val settings: SettingsDataStore
) {

    private companion object {
        const val TAG = "SaveTrackLanguageUseCase"
    }

    suspend operator fun invoke(track: PlayerTrack) {

        val language = track.language ?: return // Nothing to save (e.g. PlayerTrack.NO_SUBTITLES)

        when (track.type) {
            PlayerTrack.Type.SUBTITLES -> settings.setSubtitlesLanguage(language)
            PlayerTrack.Type.AUDIO -> settings.setAudioLanguage(language)
        }

    }

    suspend fun getSubtitlesLanguage(): String = settings.flow.first().subtitlesLanguage

    suspend fun getAudioLanguage(): String = settings.flow.first().audioLanguage

}