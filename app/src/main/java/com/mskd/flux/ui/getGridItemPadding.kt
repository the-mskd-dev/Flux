package com.mskd.flux.ui

import androidx.compose.foundation.layout.PaddingValues
import com.mskd.flux.ui.theme.FluxUI

fun getGridItemPadding(index: Int, columns: Int) : PaddingValues {
    val columnIndex = index % columns
    val isFirstColumn = columnIndex == 0
    val isLastColumn = columnIndex == columns - 1

    return when {
        isFirstColumn -> FluxUI.Grid.startPadding
        isLastColumn -> FluxUI.Grid.lastPadding
        else -> FluxUI.Grid.middlePadding
    }
}