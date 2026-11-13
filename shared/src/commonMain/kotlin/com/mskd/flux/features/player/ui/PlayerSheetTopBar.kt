package com.mskd.flux.features.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI

/**
 * Top bar of the player settings sheets: a back button, then the title on the left, followed by a divider.
 * Same layout as the top bar of PlayerMediaInfoSheet, so every sheet reads the same.
 */
@Composable
fun PlayerSheetTopBar(
    title: String,
    onBack: () -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = FluxUI.Space.small, end = FluxUI.Space.medium, top = FluxUI.Space.small, bottom = FluxUI.Space.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "back button"
            )
        }

        Text.TopBar.Title(
            modifier = Modifier.weight(1f),
            text = title,
        )

    }

    HorizontalDivider(modifier = Modifier.fillMaxWidth())

}
