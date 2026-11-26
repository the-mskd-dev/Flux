package com.mskd.flux.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.text.Text

@Composable
fun InfoChip(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
) {

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(
                vertical = Design.Space.small,
                horizontal = Design.Space.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Text.Button.Chip(
            text = text,
            color = contentColor
        )
    }
}

@Preview
@Composable
fun InfoChip_Preview() {
    MaterialTheme {
        Box(modifier = Modifier.width(200.dp)) {
            FlowRow(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Design.Space.extraSmall),
                horizontalArrangement = Arrangement.spacedBy(Design.Space.extraSmall),
            ) {
                listOf(
                    "Action & Adventure",
                    "Animation",
                    "Drama",
                    "Sci-Fi & Fantasy"
                ).forEach { InfoChip(text = it) }
            }
        }
    }
}