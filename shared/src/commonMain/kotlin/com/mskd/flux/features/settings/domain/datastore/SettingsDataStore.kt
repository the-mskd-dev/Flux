package com.mskd.flux.features.settings.domain.datastore

import com.mskd.flux.core.model.language.Language
import com.mskd.flux.system.systemLanguage
import kotlinx.coroutines.flow.Flow

interface SettingsDataStore {

    val flow: Flow<State>

    suspend fun setPlayerRewindValue(value: Int)

    suspend fun setPlayerForwardValue(value: Int)

    suspend fun setDataLanguage(language: Language?)

    suspend fun setSubtitlesLanguage(language: String)

    suspend fun setAudioLanguage(language: String)

    suspend fun setExternalPlayer(useExternalPlayer: Boolean)
    suspend fun externalPlayerIsEnabled() : Boolean

    suspend fun setEnablePip(enable: Boolean)

    suspend fun setPrefetchHdImages(prefetch: Boolean)

    suspend fun setAutoKeyboard(autoKeyboard: Boolean)

    suspend fun getDataLanguage() : String

    suspend fun setSystemFolders(enabled: Boolean)


    data class State(
        val playerRewindValue: Int = 10,
        val playerForwardValue: Int = 10,
        val subtitlesLanguage: String = systemLanguage(),
        val audioLanguage: String = systemLanguage(),
        val externalPlayer: Boolean = false,
        val pipIsEnabled: Boolean = true,
        val autoKeyboard: Boolean = true,
        val dataLanguage: Language? = null,
        val prefetchHdImages: Boolean = false,
        val systemFoldersEnabled: Boolean = true,
    )

}