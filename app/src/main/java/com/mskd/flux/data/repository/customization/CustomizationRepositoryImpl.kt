package com.mskd.flux.data.repository.customization

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mskd.flux.ui.theme.Ui
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class CustomizationRepositoryImpl(
    val customizationDataStore: DataStore<Preferences>
) : CustomizationRepository {

    object Keys {
        val UI_THEME = stringPreferencesKey("ui_theme")
        val COLOR = intPreferencesKey("color")
        val WAVE_PROGRESS = booleanPreferencesKey("wave_progress")
        val OLD_BLURRED_HEADER = booleanPreferencesKey("old_blurred_header")
        val LARGE_EPISODE_IMAGE = booleanPreferencesKey("large_episode_image")
        val ITEMS_PER_ROW = intPreferencesKey("items_per_row")
    }

    override val flow: Flow<CustomizationRepository.State> = customizationDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->

            val uiTheme = preferences[Keys.UI_THEME]?.let { Ui.THEME.valueOf(it) } ?: Ui.THEME.SYSTEM
            val color = preferences[Keys.COLOR]
            val waveProgress = preferences[Keys.WAVE_PROGRESS] ?: true
            val oldBlurredHeader = preferences[Keys.OLD_BLURRED_HEADER] ?: false
            val largeEpisodeImage = preferences[Keys.LARGE_EPISODE_IMAGE] ?: false
            val itemsPerRow = preferences[Keys.ITEMS_PER_ROW] ?: 3

            CustomizationRepository.State(
                uiTheme = uiTheme,
                color = color,
                waveProgress = waveProgress,
                oldBlurredHeader = oldBlurredHeader,
                largeEpisodeImage = largeEpisodeImage,
                itemsPerRow = itemsPerRow
            )
        }

    override suspend fun setUiTheme(theme: Ui.THEME) {
        customizationDataStore.edit { preferences ->
            preferences[Keys.UI_THEME] = theme.toString()
        }
    }

    override suspend fun setColor(color: Int?) {
        customizationDataStore.edit { preferences ->
            if (color == null)
                preferences.remove(Keys.COLOR)
            else
                preferences[Keys.COLOR] = color
        }
    }

    override suspend fun setWaveProgress(waveProgress: Boolean) {
        customizationDataStore.edit { preferences ->
            preferences[Keys.WAVE_PROGRESS] = waveProgress
        }
    }

    override suspend fun setOldBlurredHeader(blurred: Boolean) {
        customizationDataStore.edit { preferences ->
            preferences[Keys.OLD_BLURRED_HEADER] = blurred
        }
    }

    override suspend fun setLargeEpisodeImage(large: Boolean) {
        customizationDataStore.edit { preferences ->
            preferences[Keys.LARGE_EPISODE_IMAGE] = large
        }
    }

    override suspend fun setItemsPerRow(count: Int) {
        customizationDataStore.edit { preferences ->
            preferences[Keys.ITEMS_PER_ROW] = count
        }
    }

}