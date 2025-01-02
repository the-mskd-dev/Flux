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
import com.kaem.flux.ui.theme.Dimensions

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
        fontWeight = Dimensions.Weight.BOLD,
        fontSize = Dimensions.FontSize.TITLE,
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
    fontSize: TextUnit = Dimensions.FontSize.LARGE,
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
        fontWeight = Dimensions.Weight.BOLD,
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
    fontSize: TextUnit = Dimensions.FontSize.MEDIUM,
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
        fontWeight = Dimensions.Weight.MEDIUM,
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
    fontSize: TextUnit = Dimensions.FontSize.MEDIUM,
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
        fontWeight = Dimensions.Weight.LIGHT,
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
        fontWeight = Dimensions.Weight.LIGHT,
        fontSize = Dimensions.FontSize.SMALL,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}