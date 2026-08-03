package com.mskd.flux.ui.component.global

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object Text {

    object Style {

        @Composable fun topbarTitle() = MaterialTheme.typography.headlineSmall
        @Composable fun mainTitle() = MaterialTheme.typography.displaySmallEmphasized
        @Composable fun contentTitle() = MaterialTheme.typography.titleLargeEmphasized
        @Composable fun contentBody() = MaterialTheme.typography.bodyLarge
        @Composable fun contentLabel() = MaterialTheme.typography.labelMedium
        @Composable fun cardTitle() = MaterialTheme.typography.titleMediumEmphasized
        @Composable fun cardBody() = MaterialTheme.typography.bodyMedium
        @Composable fun cardLabel() = MaterialTheme.typography.bodySmall
        @Composable fun listSection() = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        @Composable fun listTitle() = MaterialTheme.typography.bodyLarge
        @Composable fun listBody() = MaterialTheme.typography.bodyMedium
        @Composable fun button() = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum")

    }

    @Composable
    fun Adaptive(
        text: String?,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = Color.Unspecified,
        style: TextStyle = LocalTextStyle.current,
        overflow: TextOverflow = TextOverflow.Clip,
        maxLines: Int = Int.MAX_VALUE,
        minLines: Int = 1,
        autoSize: TextAutoSize? = null,
    ) {

        if (text.isNullOrBlank())
            return

        val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

        var titleFontSize by remember { mutableStateOf(style.fontSize) }

        val onTextLayout : ((TextLayoutResult) -> Unit)? = if (autoSize != null) {
            { result ->
                val calculatedSize = result.layoutInput.style.fontSize
                if (calculatedSize != titleFontSize)
                    titleFontSize = calculatedSize
            }
        } else null

        Text(
            modifier = modifier,
            text = text,
            color = textColor,
            style = style.copy(lineHeight = titleFontSize * 1.2f),
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines,
            autoSize = autoSize,
            onTextLayout = onTextLayout
        )

    }

    @Composable
    fun Annotated(
        text: AnnotatedString,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Start,
        color: Color = Color.Unspecified,
        style: TextStyle = MaterialTheme.typography.bodyLarge,
        overflow: TextOverflow = TextOverflow.Clip,
        maxLines: Int = Int.MAX_VALUE,
        minLines: Int = 1,
    ) {

        if (text.isBlank())
            return

        val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

        Text(
            modifier = modifier,
            text = text,
            color = textColor,
            style = style,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines
        )

    }

    object TopBar {

        @Composable
        fun Title(
            text: String?,
            modifier: Modifier = Modifier,
            color: Color = Color.Unspecified,
        ) {

            Adaptive(
                modifier = modifier,
                text = text,
                overflow = TextOverflow.Ellipsis,
                style = Style.topbarTitle(),
                color = color,
                maxLines = 2,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = Style.topbarTitle().fontSize,
                    minFontSize = 14.sp
                )
            )

        }

    }

    @Composable
    fun MainTitle(
        text: String?,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
        textAlign: TextAlign = TextAlign.Start,
    ) {

        if (text.isNullOrBlank())
            return

        val style = Style.mainTitle()
        val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

        Adaptive(
            modifier = modifier,
            text = text,
            color = textColor,
            style = Style.mainTitle(),
            maxLines = 2,
            textAlign = textAlign,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = Style.mainTitle().fontSize,
                minFontSize = 14.sp,
            )
        )

    }

    object Content {

        @Composable
        fun Title(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = Style.contentTitle()
            val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

            Text(
                modifier = modifier,
                text = text,
                color = textColor,
                style = style,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout
            )

        }

        @Composable
        fun Body(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = Style.contentBody()
            val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

            Text(
                modifier = modifier,
                text = text,
                color = textColor,
                style = style,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout
            )

        }

        @Composable
        fun Label(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = Style.contentLabel()
            val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

            Text(
                modifier = modifier,
                text = text,
                color = textColor,
                style = style,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                fontWeight = FontWeight.Bold,
                onTextLayout = onTextLayout
            )

        }



    }

    object Card {

        @Composable
        fun Title(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = Style.cardTitle()
            val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

            Text(
                modifier = modifier,
                text = text,
                color = textColor,
                style = style,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout
            )

        }

        @Composable
        fun Body(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = Style.cardBody()
            val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

            Text(
                modifier = modifier,
                text = text,
                color = textColor,
                style = style,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout
            )

        }

        @Composable
        fun Label(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = Style.cardLabel()
            val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

            Text(
                modifier = modifier,
                text = text,
                color = textColor,
                style = style,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout
            )

        }

    }

    object List {

        @Composable
        fun Section(
            text: String?,
            modifier: Modifier = Modifier,
            color: Color = Color.Unspecified,
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                text = text,
                color = color,
                modifier = modifier,
                maxLines = 1,
                style = Style.listSection(),
            )

        }

        @Composable
        fun Title(
            text: String?,
            modifier: Modifier = Modifier,
            color: Color = Color.Unspecified,
            textAlign: TextAlign? = null,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                text = text,
                color = color,
                modifier = modifier,
                textAlign = textAlign,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                style = Style.listTitle(),
            )

        }

        @Composable
        fun Body(
            text: String?,
            modifier: Modifier = Modifier,
            color: Color = Color.Unspecified,
            autoSize: TextAutoSize? = null,
            fontStyle: FontStyle? = null,
            textDecoration: TextDecoration? = null,
            textAlign: TextAlign? = null,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                text = text,
                color = color,
                modifier = modifier,
                autoSize = autoSize,
                fontStyle = fontStyle,
                textDecoration = textDecoration,
                textAlign = textAlign,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                style = Style.listBody(),
            )

        }

    }

    @Composable
    fun Button(
        text: String,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
    ) {

        Text(
            modifier = modifier,
            text = text,
            color = color,
            style = Style.button()
        )

    }

}