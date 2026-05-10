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
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.Ui

@Composable
fun SettingsSwitch(
    text: String,
    subText: String,
    checked: Boolean,
    painter: Painter,
    backgroundColor: Color,
    iconColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(all = Ui.Space.MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
    ) {

        SettingIcon(
            painter = painter,
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            contentDescription = text
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = Ui.Space.MEDIUM),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {

            Text.Title.Medium(
                text = text,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text.Title.Small(
                text = subText,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
                lineHeight = 18.sp
            )

        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )

    }

}