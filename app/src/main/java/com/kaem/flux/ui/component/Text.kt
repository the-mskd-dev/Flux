package com.kaem.flux.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight

@Composable
fun Title(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = Modifier.then(modifier),
        text = text,
        color = color,
        fontWeight = FluxWeight.BOLD,
        fontSize = FluxFontSize.TITLE,
        textAlign = textAlign
    )

}

@Composable
fun MediumText(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontWeight = FluxWeight.BOLD,
        fontSize = FluxFontSize.LARGE,
        textAlign = textAlign
    )

}