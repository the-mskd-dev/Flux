package com.mskd.flux.features.customization.domain.datastore

import com.mskd.flux.features.customization.domain.model.NavigationStyle
import com.mskd.flux.utils.UiCommon
import kotlinx.coroutines.flow.Flow

interface CustomizationDataStore {

    val flow: Flow<State>

    suspend fun setUiTheme(theme: UiCommon.THEME)

    suspend fun setColor(color: Int?)

    suspend fun setWaveProgress(waveProgress: Boolean)
    suspend fun setOldBlurredHeader(blurred: Boolean)
    suspend fun setLargeEpisodeImage(large: Boolean)

    suspend fun setItemsPerRow(count: Int)
    suspend fun setItemsCorners(corners: Int)
    suspend fun setSeasonsPerRow(count: Int)
    suspend fun setNavigationStyle(style: NavigationStyle)

    data class State(
        val uiTheme: UiCommon.THEME = UiCommon.THEME.SYSTEM,
        val color: Int? = null,
        val waveProgress: Boolean = true,
        val oldBlurredHeader: Boolean = false,
        val largeEpisodeImage: Boolean = false,
        val itemsPerRow: Int = 3,
        val itemsCorners: Int = 12,
        val seasonsPerRow: Int = 3,
        val navigationStyle: NavigationStyle = NavigationStyle.PILL
    )

}