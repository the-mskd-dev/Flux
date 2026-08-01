package com.mskd.flux.screens.settings.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.mskd.flux.screens.settings.SettingIcon
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.uppercaseFirstLetter

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsItem(
    text: String,
    subText: String? = null,
    painter: Painter? = null,
    iconColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    iconBackgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 1f),
    valueColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
    onTap: () -> Unit
) {

    ListItem(
        onClick = onTap,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        content = {
            Text.List.Title(text = text)
        },
        supportingContent = {
            AnimatedContent(
                targetState = subText.uppercaseFirstLetter()
            ) { text ->
                Text.List.Content(
                    text = text,
                    color = valueColor,
                )
            }
        },
        leadingContent = painter?.let {
            {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    SettingIcon(
                        painter = it,
                        backgroundColor = iconBackgroundColor,
                        iconColor = iconColor,
                        contentDescription = text
                    )
                }

            }
        }
    )

    
}