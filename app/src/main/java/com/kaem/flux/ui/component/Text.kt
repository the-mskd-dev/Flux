package com.kaem.flux.ui.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.kaem.flux.ui.theme.Ui

@Composable
fun TextBold(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
    fontSize: TextUnit = Ui.FontSize.LARGE,
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
        fontWeight = Ui.Weight.BOLD,
        fontSize = fontSize,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun TextSmall(
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
        fontWeight = Ui.Weight.MEDIUM,
        fontSize = Ui.FontSize.SMALL,
        textAlign = textAlign,
        fontStyle = fontStyle,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextHeadline(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        style = MaterialTheme.typography.headlineLargeEmphasized,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextTitleLarge(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        style = MaterialTheme.typography.titleLargeEmphasized,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextTitleSmall(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        style = MaterialTheme.typography.titleSmall,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun TextBodyLarge(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        style = MaterialTheme.typography.bodyLarge,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun TextLabelLarge(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        style = MaterialTheme.typography.labelLarge,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}

@Composable
fun TextLabelSmall(
    modifier: Modifier = Modifier,
    text: String?,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground,
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
        style = MaterialTheme.typography.labelSmall,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines
    )

}