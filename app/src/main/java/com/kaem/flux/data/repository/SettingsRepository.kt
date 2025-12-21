package com.kaem.flux.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

val Context.settingsDatastore by preferencesDataStore(
    name = "SettingsDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

data class SettingsPreferences(
    val playerRewindValue: Int = 10,
    val playerForwardValue: Int = 10,
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val subtitlesLanguage: Locale = Locale.getDefault()
)

class SettingsRepository @Inject constructor(
    val settingsDataStore: DataStore<Preferences>
) {

    object Keys {
        val PLAYER_REWIND = intPreferencesKey("player_rewind")
        val PLAYER_FORWARD = intPreferencesKey("player_forward")
        val UI_THEME = stringPreferencesKey("ui_theme")
        val SUBTITLES_LANGUAGE = stringPreferencesKey("subtitles_language")
    }

    val flow: Flow<SettingsPreferences> = settingsDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val playerRewindValue = preferences[Keys.PLAYER_REWIND] ?: 10
            val playerForwardValue = preferences[Keys.PLAYER_FORWARD] ?: 10
            val uiTheme = preferences[Keys.UI_THEME]?.let { Ui.THEME.valueOf(it) } ?: Ui.THEME.SYSTEM
            val subtitlesLanguage = preferences[Keys.SUBTITLES_LANGUAGE]?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()

            SettingsPreferences(
                playerRewindValue = playerRewindValue,
                playerForwardValue = playerForwardValue,
                uiTheme = uiTheme,
                subtitlesLanguage = subtitlesLanguage
            )
        }

    suspend fun setPlayerRewindValue(value: Int) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.PLAYER_REWIND] = value
        }
    }


    suspend fun setPlayerForwardValue(value: Int) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.PLAYER_FORWARD] = value
        }
    }

    suspend fun setUiTheme(theme: Ui.THEME) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.UI_THEME] = theme.toString()
        }
    }

    suspend fun setSubtitlesLanguage(locale: Locale) {
        settingsDataStore.edit { preferences ->
            preferences[Keys.SUBTITLES_LANGUAGE] = locale.language
        }
    }

}