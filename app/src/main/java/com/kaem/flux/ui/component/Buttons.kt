package com.kaem.flux.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FluxButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {

    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        onClick = onClick
    ) {

        Text(text = text)

    }

}