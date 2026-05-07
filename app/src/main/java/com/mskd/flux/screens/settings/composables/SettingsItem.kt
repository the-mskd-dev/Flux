package com.mskd.flux.screens.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import com.mskd.flux.screens.settings.SettingIcon
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.extensions.uppercaseFirstLetter

@Composable
fun SettingsItem(
    text: String,
    value: String,
    painter: Painter,
    iconBackgroundColor: Color,
    iconColor: Color,
    onTap: () -> Unit
) {

    Row(
        modifier = Modifier
            .clickable { onTap() }
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SettingIcon(
            painter = painter,
            backgroundColor = iconBackgroundColor,
            iconColor = iconColor,
            contentDescription = text
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {

            Text.Title.Medium(
                text = text,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text.Title.Small(
                text = value.uppercaseFirstLetter(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
            )

        }

    }

}