package com.mskd.flux.screens.settings.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.mskd.flux.ui.theme.Ui

@Composable
fun SettingsSection(
    iconColor: Color,
    iconBackgroundColor: Color,
    content: @Composable (Color, Color) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.LARGE)
            .clip(Ui.Shape.Corner.Medium),
        horizontalAlignment = Alignment.Start
    ) { content(iconColor, iconBackgroundColor) }
}