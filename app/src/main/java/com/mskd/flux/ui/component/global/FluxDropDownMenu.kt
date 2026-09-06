package com.mskd.flux.ui.component.global

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FluxDropDownMenu(
    onDismissRequest: () -> Unit,
    items: List<FluxDropDownMenuItem>
) {

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        containerColor = Color.Transparent,
        shadowElevation = 0.dp,
        content = {

            Column(
                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {

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

        }
    )

}

data class FluxDropDownMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val leadingIcon:  @Composable (() -> Unit)
)