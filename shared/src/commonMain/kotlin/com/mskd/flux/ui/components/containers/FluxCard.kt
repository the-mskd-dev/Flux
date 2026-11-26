package com.mskd.flux.ui.components.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.LocalUiShapes
import com.mskd.flux.ui.expressive.expressivePress
import com.mskd.flux.ui.text.Text

@Composable
fun FluxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: Color = MaterialTheme.colorScheme.surface,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = modifier
            .clip(LocalUiShapes.current.corners)
            .expressivePress(enabled = onClick != null)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(background)
            .padding(
                vertical = Design.Space.large,
                horizontal = Design.Space.medium
            ),
        content = content
    )

}

@Preview
@Composable
fun FluxCard_Preview() {
    MaterialTheme {
        Box(
            modifier = Modifier.padding(Design.Space.medium),
            contentAlignment = Alignment.Center
        ) {
            FluxCard {
                Column {
                    Text.Content.Title("Card title")
                    Text.Content.Body("Some content body")
                }

            }
        }
    }
}