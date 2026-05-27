package com.mskd.flux.data.repository.customization

import android.content.Context
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

    data class State(
        val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
        val color: Int? = null,
        val waveProgress: Boolean = true,
    )

}