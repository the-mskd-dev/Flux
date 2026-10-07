package com.mskd.flux.ui.modal.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.mskd.flux.ui.global.BackGesture
import com.mskd.flux.ui.modal.FluxScrim
import com.mskd.flux.ui.text.Text

@Composable
fun FluxPopUpMenu(
    onDismissRequest: () -> Unit,
    items: List<FluxPopUpMenuItem>
) {

    if (items.isEmpty())
        return

    FluxScrim(
        isVisible = true,
        onDismiss = onDismissRequest
    )

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        shadowElevation = 0.dp,
        shape = MaterialTheme.shapes.extraLarge,
        properties = PopupProperties(focusable = true, dismissOnBackPress = false),
        content = {

            items.forEach { item ->

                DropdownMenuItem(
                    modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer),
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        leadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    onClick = item.onClick,
                    text = { Text.Card.Body(text = item.text) },
                    leadingIcon = item.leadingIcon,
                )

            }

        }
    )

}