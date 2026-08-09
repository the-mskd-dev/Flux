package com.mskd.flux.screens.search.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.ui.component.global.Text

@Composable
fun SearchFilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {

    FilterChip(
        onClick = onClick,
        label = { Text.Button.Chip(text = text) },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "$text selected",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else { null },
    )

}