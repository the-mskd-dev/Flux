package com.kaem.flux.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxWeight

@Composable
fun Title(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = Modifier.then(modifier),
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FluxWeight.BOLD,
        fontSize = FluxFontSize.TITLE,
        textAlign = textAlign
    )

}