package com.kaem.flux.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxWeight

@Composable
fun Title(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontStyle: FontStyle? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = Modifier.then(modifier),
        text = text,
        color = color,
        fontWeight = FluxWeight.BOLD,
        fontSize = FluxFontSize.TITLE,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun BoldText(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = FluxFontSize.LARGE,
    fontStyle: FontStyle? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontWeight = FluxWeight.BOLD,
        fontSize = fontSize,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun MediumText(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = FluxFontSize.MEDIUM,
    fontStyle: FontStyle? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontWeight = FluxWeight.MEDIUM,
        fontSize = fontSize,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun LightText(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = FluxFontSize.MEDIUM,
    fontStyle: FontStyle? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontWeight = FluxWeight.LIGHT,
        fontSize = fontSize,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun SmallText(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontStyle: FontStyle? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
) {

    if (text.isNullOrBlank())
        return

    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontWeight = FluxWeight.LIGHT,
        fontSize = FluxFontSize.SMALL,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}