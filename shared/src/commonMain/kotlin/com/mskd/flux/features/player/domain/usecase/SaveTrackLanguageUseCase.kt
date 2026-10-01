package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.flow.first
import java.util.Locale

class SaveTrackLanguageUseCase(
    private val settings: SettingsDataStore
) {

    private companion object {
        const val TAG = "SaveTrackLanguageUseCase"
    }

    suspend operator fun invoke(track: PlayerTrack) {

        val language = track.language ?: return // Nothing to save (e.g. PlayerTrack.NO_SUBTITLES)

        try {
            val locale = Locale.forLanguageTag(language)

            when (track.type) {
                PlayerTrack.Type.SUBTITLES -> settings.setSubtitlesLanguage(locale)
                PlayerTrack.Type.AUDIO -> settings.setAudioLanguage(locale)
            }

        } catch (e: Exception) {
            Trace.error(tag = TAG, message = "Locale not found for $language", throwable = e)
        }
    }

    suspend fun getSubtitlesLanguage(): Locale = settings.flow.first().subtitlesLanguage

    suspend fun getAudioLanguage(): Locale = settings.flow.first().audioLanguage

}