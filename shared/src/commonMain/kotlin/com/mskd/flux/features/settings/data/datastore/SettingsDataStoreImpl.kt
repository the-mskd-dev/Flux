package com.mskd.flux.features.settings.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.setup.domain.model.SourceSelectionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Locale

class SettingsDataStoreImpl(val settingsDataStore: DataStore<Preferences>) : SettingsDataStore {

    object Keys {
        val PLAYER_REWIND = intPreferencesKey("player_rewind")
        val PLAYER_FORWARD = intPreferencesKey("player_forward")
        val SUBTITLES_LANGUAGE = stringPreferencesKey("subtitles_language")
        val AUDIO_LANGUAGE = stringPreferencesKey("audio_language")
        val EXTERNAL_PLAYER = booleanPreferencesKey("external_player")
        val PIP_IS_ENABLED = booleanPreferencesKey("pip_is_enabled")
        val AUTO_KEYBOARD = booleanPreferencesKey("auto_keyboard_in_search")
        val DATA_LANGUAGE = stringPreferencesKey("data_language")
        val PREFETCH_IMAGES = booleanPreferencesKey("prefetch_hd_images")
        val SOURCE_SELECTION_MODE = stringPreferencesKey("source_selection_mode")
    }

    override val flow: Flow<SettingsDataStore.State> = settingsDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val playerRewindValue = preferences[Keys.PLAYER_REWIND] ?: 10
            val playerForwardValue = preferences[Keys.PLAYER_FORWARD] ?: 10
            val subtitlesLanguage = preferences[Keys.SUBTITLES_LANGUAGE]?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
            val audioLanguage = preferences[Keys.AUDIO_LANGUAGE]?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
            val externalPlayer = preferences[Keys.EXTERNAL_PLAYER] ?: false
            val pipIsEnabled = preferences[Keys.PIP_IS_ENABLED] ?: true
            val autoKeyboard = preferences[Keys.AUTO_KEYBOARD] ?: true
            val dataLanguage = preferences[Keys.DATA_LANGUAGE]?.let { Locale.forLanguageTag(it) }
            val prefetchImages = preferences[Keys.PREFETCH_IMAGES] ?: false
            val sourceSelectionMode = preferences[Keys.SOURCE_SELECTION_MODE]?.let { SourceSelectionMode.valueOf(it) } ?: SourceSelectionMode.DEFAULT

            SettingsDataStore.State(
                playerRewindValue = playerRewindValue,
                playerForwardValue = playerForwardValue,
                subtitlesLanguage = subtitlesLanguage,
                audioLanguage = audioLanguage,
                externalPlayer = externalPlayer,
                pipIsEnabled = pipIsEnabled,
                autoKeyboard = autoKeyboard,
                dataLanguage = dataLanguage,
                prefetchHdImages = prefetchImages,
                sourceSelectionMode = sourceSelectionMode
            )
        }

    override suspend fun setPlayerRewindValue(value: Int) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.PLAYER_REWIND] = value
        }
    }


    override suspend fun setPlayerForwardValue(value: Int) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.PLAYER_FORWARD] = value
        }
    }

    override suspend fun setDataLanguage(locale: Locale?) {
        settingsDataStore.edit { preferences ->
            if (locale != null)
                preferences[Keys.DATA_LANGUAGE] = locale.language
            else
                preferences.remove(Keys.DATA_LANGUAGE)
        }
    }

    override suspend fun getDataLanguage(): Locale {
        return flow.firstOrNull()?.dataLanguage ?: Locale.getDefault()
    }

    override suspend fun setSourceSelectionMode(mode: SourceSelectionMode) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.SOURCE_SELECTION_MODE] = mode.toString()
        }
    }

    override suspend fun setSubtitlesLanguage(locale: Locale) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.SUBTITLES_LANGUAGE] = locale.language
        }
    }

    override suspend fun setAudioLanguage(locale: Locale) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.AUDIO_LANGUAGE] = locale.language
        }
    }

    override suspend fun setExternalPlayer(useExternalPlayer: Boolean) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.EXTERNAL_PLAYER] = useExternalPlayer
        }
    }

    override suspend fun setEnablePip(enable: Boolean) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.PIP_IS_ENABLED] = enable
        }
    }

    override suspend fun setAutoKeyboard(autoKeyboard: Boolean) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.AUTO_KEYBOARD] = autoKeyboard
        }
    }

    override suspend fun setPrefetchHdImages(prefetch: Boolean) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.PREFETCH_IMAGES] = prefetch
        }
    }

}