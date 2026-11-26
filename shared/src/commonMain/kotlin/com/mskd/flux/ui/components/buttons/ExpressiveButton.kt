package com.mskd.flux.ui.components.buttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ExpressiveButton(
    text: String,
    leading: @Composable (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {

}

@Preview
@Composable
fun ExpressiveButton_Preview() {
    MaterialTheme {
        ExpressiveButton(
            text = "ExpressiveButton"
        )
    }
}