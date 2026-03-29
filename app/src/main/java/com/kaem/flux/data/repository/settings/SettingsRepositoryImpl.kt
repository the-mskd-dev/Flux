package com.kaem.flux.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    val settingsDataStore: DataStore<Preferences>
) : SettingsRepository {

    object Keys {
        val PLAYER_REWIND = intPreferencesKey("player_rewind")
        val PLAYER_FORWARD = intPreferencesKey("player_forward")
        val UI_THEME = stringPreferencesKey("ui_theme")
        val SUBTITLES_LANGUAGE = stringPreferencesKey("subtitles_language")
        val AUDIO_LANGUAGE = stringPreferencesKey("audio_language")
    }

    override val flow: Flow<SettingsRepository.State> = settingsDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val playerRewindValue = preferences[Keys.PLAYER_REWIND] ?: 10
            val playerForwardValue = preferences[Keys.PLAYER_FORWARD] ?: 10
            val uiTheme = preferences[Keys.UI_THEME]?.let { Ui.THEME.valueOf(it) } ?: Ui.THEME.SYSTEM
            val subtitlesLanguage = preferences[Keys.SUBTITLES_LANGUAGE]?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
            val audioLanguage = preferences[Keys.AUDIO_LANGUAGE]?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()

            SettingsRepository.State(
                playerRewindValue = playerRewindValue,
                playerForwardValue = playerForwardValue,
                uiTheme = uiTheme,
                subtitlesLanguage = subtitlesLanguage,
                audioLanguage = audioLanguage
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

    override suspend fun setUiTheme(theme: Ui.THEME) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.UI_THEME] = theme.toString()
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

}