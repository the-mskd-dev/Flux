package com.kaem.flux.data.repository.settings

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
    val subtitlesLanguage: Locale = Locale.getDefault(),
    val audioLanguage: Locale = Locale.getDefault()
)

interface SettingsRepository {

    val flow: Flow<SettingsPreferences>

    suspend fun setPlayerRewindValue(value: Int)

    suspend fun setPlayerForwardValue(value: Int)

    suspend fun setUiTheme(theme: Ui.THEME)

    suspend fun setSubtitlesLanguage(locale: Locale)

    suspend fun setAudioLanguage(locale: Locale)

}