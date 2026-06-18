package com.mskd.flux.data.repository.customization

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.mskd.flux.ui.theme.Ui
import kotlinx.coroutines.flow.Flow

val Context.customizationDatastore by preferencesDataStore(
    name = "CustomizationDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

interface CustomizationRepository {

    val flow: Flow<State>

    suspend fun setUiTheme(theme: Ui.THEME)

    suspend fun setColor(color: Int?)

    suspend fun setWaveProgress(waveProgress: Boolean)
    suspend fun setOldBlurredHeader(blurred: Boolean)
    suspend fun setLargeEpisodeImage(large: Boolean)

    suspend fun setItemsPerRow(count: Int)

    data class State(
        val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
        val color: Int? = null,
        val waveProgress: Boolean = true,
        val oldBlurredHeader: Boolean = false,
        val largeEpisodeImage: Boolean = false,
        val itemsPerRow: Int = 3
    )

}

val LocalCustomization = compositionLocalOf { CustomizationRepository.State() }