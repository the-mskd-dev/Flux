package com.mskd.flux.features.settings.domain.datastore

import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface SettingsDataStore {

    val flow: Flow<State>

    suspend fun setPlayerRewindValue(value: Int)

    suspend fun setPlayerForwardValue(value: Int)

    suspend fun setDataLanguage(locale: Locale?)

    suspend fun setSubtitlesLanguage(locale: Locale)

    suspend fun setAudioLanguage(locale: Locale)

    suspend fun setExternalPlayer(useExternalPlayer: Boolean)

    suspend fun setEnablePip(enable: Boolean)

    suspend fun setPrefetchHdImages(prefetch: Boolean)

    suspend fun setAutoKeyboard(autoKeyboard: Boolean)

    suspend fun getDataLanguage() : Locale

    data class State(
        val playerRewindValue: Int = 10,
        val playerForwardValue: Int = 10,
        val subtitlesLanguage: Locale = Locale.getDefault(),
        val audioLanguage: Locale = Locale.getDefault(),
        val externalPlayer: Boolean = false,
        val pipIsEnabled: Boolean = true,
        val autoKeyboard: Boolean = true,
        val dataLanguage: Locale? = null,
        val prefetchHdImages: Boolean = false
    )
}