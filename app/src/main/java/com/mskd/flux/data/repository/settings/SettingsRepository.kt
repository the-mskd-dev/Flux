package com.mskd.flux.data.repository.settings

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.mskd.flux.ui.theme.Ui
import kotlinx.coroutines.flow.Flow
import java.util.Locale

val Context.settingsDatastore by preferencesDataStore(
    name = "SettingsDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

interface SettingsRepository {

    val flow: Flow<State>

    suspend fun setPlayerRewindValue(value: Int)

    suspend fun setPlayerForwardValue(value: Int)

    suspend fun setUiTheme(theme: Ui.THEME)

    suspend fun setSubtitlesLanguage(locale: Locale)

    suspend fun setAudioLanguage(locale: Locale)

    data class State(
        val playerRewindValue: Int = 10,
        val playerForwardValue: Int = 10,
        val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
        val subtitlesLanguage: Locale = Locale.getDefault(),
        val audioLanguage: Locale = Locale.getDefault()
    )
}