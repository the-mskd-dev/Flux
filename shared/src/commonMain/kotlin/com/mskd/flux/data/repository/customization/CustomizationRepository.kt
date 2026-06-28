package com.mskd.flux.data.repository.customization

import com.mskd.flux.utils.UiCommon
import kotlinx.coroutines.flow.Flow


interface CustomizationRepository {

    val flow: Flow<State>

    suspend fun setUiTheme(theme: UiCommon.THEME)

    suspend fun setColor(color: Int?)

    suspend fun setWaveProgress(waveProgress: Boolean)
    suspend fun setOldBlurredHeader(blurred: Boolean)
    suspend fun setLargeEpisodeImage(large: Boolean)

    suspend fun setItemsPerRow(count: Int)
    suspend fun setItemsCorners(corners: Int)
    suspend fun setSeasonsPerRow(count: Int)

    data class State(
        val uiTheme: UiCommon.THEME = UiCommon.THEME.SYSTEM,
        val color: Int? = null,
        val waveProgress: Boolean = true,
        val oldBlurredHeader: Boolean = false,
        val largeEpisodeImage: Boolean = false,
        val itemsPerRow: Int = 3,
        val itemsCorners: Int = 12,
        val seasonsPerRow: Int = 3,
    )

}