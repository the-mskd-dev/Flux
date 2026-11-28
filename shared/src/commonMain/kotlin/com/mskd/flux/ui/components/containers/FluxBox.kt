package com.mskd.flux.ui.components.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import com.mskd.flux.ui.motion.expressivePress
import com.mskd.flux.ui.text.Text

@Composable
fun FluxBox(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = modifier
            .expressivePress(enabled = onClick != null)
            .clip(LocalUiShapes.current.corners)
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
fun FluxBox_Preview() {
    MaterialTheme {
        Box(
            modifier = Modifier.padding(Design.Space.medium),
            contentAlignment = Alignment.Center
        ) {
            FluxBox(
                onClick = {}
            ) {
                Column {
                    Text.Content.Title("Card title")
                    Text.Content.Body("Some content body")
                }

            }
        }
    }
}