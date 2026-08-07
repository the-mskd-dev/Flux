package com.mskd.flux.ui.component.global

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun FluxBottomSheetItem(
    isSelected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onClick() }
        )
        Text.List.Body(
            modifier = Modifier.weight(1f),
            text = text
        )
    }
}