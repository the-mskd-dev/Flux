package com.kaem.flux.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaem.flux.ui.theme.Ui

@Composable
fun FluxButton(
    modifier: Modifier = Modifier,
    text: String,
    autoSize: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    border: BorderStroke? = null,
    onTap: () -> Unit
) {

    Button(
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
        ),
        shape = Ui.Shape.RoundedCorner,
        border = border,
        onClick = onTap
    ) {

        if (autoSize) {

            var fontSize by remember { mutableStateOf(Ui.FontSize.BUTTON) }
            var readyToDraw by remember { mutableStateOf(false) }

            Text(
                modifier = modifier.drawWithContent {
                    if (readyToDraw) drawContent()
                },
                text = text,
                fontSize = fontSize,
                maxLines = 1,
                fontWeight = Ui.Weight.MEDIUM,
                softWrap = false,
                onTextLayout = {
                    if (it.didOverflowWidth)
                        fontSize = fontSize.times(.95)
                    else
                        readyToDraw = true
                }
            )
        } else {
            MediumText(
                text = text,
                color = textColor,
                fontSize = Ui.FontSize.BUTTON
            )
        }


    }

}

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    onTap: () -> Unit
) {

    Box(
        modifier = modifier
            .statusBarsPadding()
            .clickable { onTap() }
            .size(50.dp)
            .clip(shape = CircleShape)
            .padding(Ui.Space.EXTRA_SMALL),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            tint = tint,
            contentDescription = "back button"
        )

    }

}

@Composable
fun FluxTextButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onTap: () -> Unit
) {

    TextButton(
        modifier = modifier,
        onClick = onTap,
        content = {
            MediumText(
                text = text,
                color = color,
                fontSize = Ui.FontSize.BUTTON
            )
        }
    )

}