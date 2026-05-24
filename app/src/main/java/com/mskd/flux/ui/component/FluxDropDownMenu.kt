package com.mskd.flux.ui.component

import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FluxDropDownMenu(
    onDismissRequest: () -> Unit,
    items: List<FluxDropDownMenuItem>
) {

    DropdownMenu(
        shape = MaterialTheme.shapes.extraLarge,
        expanded = true,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        content = {

            items.forEach { item ->

                DropdownMenuItem(
                    modifier = Modifier.background(MaterialTheme.colorScheme.tertiaryContainer),
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        leadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ),
                    onClick = item.onClick,
                    text = { Text.Body.Medium(text = item.text) },
                    leadingIcon = item.leadingIcon,
                )

            }

        }
    )

}

data class FluxDropDownMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val leadingIcon:  @Composable (() -> Unit)
)