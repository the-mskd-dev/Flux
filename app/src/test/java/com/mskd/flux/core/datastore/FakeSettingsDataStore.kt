package com.mskd.flux.core.datastore

import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

class FakeSettingsDataStore : SettingsDataStore {

    override val flow: Flow<SettingsDataStore.State> = MutableStateFlow(SettingsDataStore.State())

    override suspend fun getDataLanguage(): Locale {
        return Locale.US
    }

    override suspend fun setPlayerRewindValue(value: Int) {}

    override suspend fun setPlayerForwardValue(value: Int) {}

    override suspend fun setDataLanguage(locale: Locale?) {}

    override suspend fun setSubtitlesLanguage(locale: Locale) {}

    override suspend fun setAudioLanguage(locale: Locale) {}

    override suspend fun setExternalPlayer(useExternalPlayer: Boolean) {}

    override suspend fun setEnablePip(enable: Boolean) {}

    override suspend fun setPrefetchHdImages(prefetch: Boolean) {}

    override suspend fun setAutoKeyboard(autoKeyboard: Boolean) {}
}