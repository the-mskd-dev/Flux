package com.mskd.flux.screens.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.mskd.flux.ui.theme.Ui

@Composable
fun SettingsSection(
    iconColor: Color,
    backgroundColor: Color,
    content: @Composable (Color, Color) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.MEDIUM)
            .clip(Ui.Shape.Corner.Small)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalAlignment = Alignment.Start
    ) { content(iconColor, backgroundColor) }
}