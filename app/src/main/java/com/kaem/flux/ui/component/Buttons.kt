package com.kaem.flux.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun FluxButton(
    modifier: Modifier = Modifier,
    text: String,
    autoSize: Boolean = false,
    onClick: () -> Unit
) {

    Button(
        modifier = Modifier.then(modifier),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        onClick = onClick
    ) {

        if (autoSize) {

            var fontSize by remember { mutableStateOf(15.sp) }
            var readyToDraw by remember { mutableStateOf(false) }

            Text(
                modifier = modifier.drawWithContent {
                    if (readyToDraw) drawContent()
                },
                text = text,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                onTextLayout = {
                    if (it.didOverflowWidth)
                        fontSize = fontSize.times(.95)
                    else
                        readyToDraw = true
                }
            )
        } else {
            Text(text = text)
        }


    }

}