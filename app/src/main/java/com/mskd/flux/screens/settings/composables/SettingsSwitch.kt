package com.mskd.flux.screens.settings.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.sp
import com.mskd.flux.screens.settings.SettingIcon
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun SettingsSwitch(
    text: String,
    subText: String? = null,
    checked: Boolean,
    painter: Painter? = null,
    iconColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    iconBackgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 1f),
    subTextColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = FluxUI.Space.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
    ) {

        painter?.let {
            SettingIcon(
                painter = it,
                backgroundColor = iconBackgroundColor,
                iconColor = iconColor,
                contentDescription = text
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = FluxUI.Space.medium),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
        ) {

            Text.Title.Medium(
                text = text,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text.Title.Small(
                text = subText,
                color = subTextColor,
                lineHeight = 18.sp
            )

        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )

    }

}