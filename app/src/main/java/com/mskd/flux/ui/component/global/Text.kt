package com.mskd.flux.ui.component.global

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object Text {

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
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                maxLines = 2,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    minFontSize = MaterialTheme.typography.titleSmall.fontSize
                )
            )

        }

    }

    @Composable
    fun MainTitle(
        text: String?,
        modifier: Modifier = Modifier,
        color: Color = Color.Unspecified,
    ) {

        if (text.isNullOrBlank())
            return

        val style = MaterialTheme.typography.displaySmallEmphasized
        val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }

        Adaptive(
            modifier = modifier,
            text = text,
            color = textColor,
            style = MaterialTheme.typography.displaySmallEmphasized,
            maxLines = 2,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MaterialTheme.typography.titleSmallEmphasized.fontSize,
                maxFontSize = MaterialTheme.typography.displaySmallEmphasized.fontSize,
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

            val style = MaterialTheme.typography.titleLargeEmphasized
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

            val style = MaterialTheme.typography.bodyLarge
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

            val style = MaterialTheme.typography.labelMedium
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

            val style = MaterialTheme.typography.titleMediumEmphasized
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

            val style = MaterialTheme.typography.bodyMediumEmphasized
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

            val style = MaterialTheme.typography.bodySmall
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
            autoSize: TextAutoSize? = null,
            fontStyle: FontStyle? = null,
            fontFamily: FontFamily? = null,
            letterSpacing: TextUnit = TextUnit.Unspecified,
            textDecoration: TextDecoration? = null,
            textAlign: TextAlign? = null,
            overflow: TextOverflow = TextOverflow.Clip,
            softWrap: Boolean = true,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout: ((TextLayoutResult) -> Unit)? = null,
            style: TextStyle = LocalTextStyle.current
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                text = text,
                color = color,
                modifier = modifier,
                autoSize = autoSize,
                fontSize = 14.sp,
                fontStyle = fontStyle,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = 20.sp,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout,
                style = style,
            )

        }

        @Composable
        fun Title(
            text: String?,
            modifier: Modifier = Modifier,
            color: Color = Color.Unspecified,
            autoSize: TextAutoSize? = null,
            fontStyle: FontStyle? = null,
            fontWeight: FontWeight = FontWeight(400),
            fontFamily: FontFamily? = null,
            letterSpacing: TextUnit = TextUnit.Unspecified,
            textDecoration: TextDecoration? = null,
            textAlign: TextAlign? = null,
            overflow: TextOverflow = TextOverflow.Clip,
            softWrap: Boolean = true,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout: ((TextLayoutResult) -> Unit)? = null,
            style: TextStyle = LocalTextStyle.current
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                text = text,
                color = color,
                modifier = modifier,
                autoSize = autoSize,
                fontSize = 16.sp,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = 24.sp,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout,
                style = style,
            )

        }

        @Composable
        fun Body(
            text: String?,
            modifier: Modifier = Modifier,
            color: Color = Color.Unspecified,
            autoSize: TextAutoSize? = null,
            fontStyle: FontStyle? = null,
            fontFamily: FontFamily? = null,
            letterSpacing: TextUnit = TextUnit.Unspecified,
            textDecoration: TextDecoration? = null,
            textAlign: TextAlign? = null,
            overflow: TextOverflow = TextOverflow.Clip,
            softWrap: Boolean = true,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout: ((TextLayoutResult) -> Unit)? = null,
            style: TextStyle = LocalTextStyle.current
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                text = text,
                color = color,
                modifier = modifier,
                autoSize = autoSize,
                fontSize = 14.sp,
                fontStyle = fontStyle,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = 20.sp,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout,
                style = style,
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
            style = ButtonDefaults.textStyleFor(buttonHeight = ButtonDefaults.MediumContainerHeight).copy(fontFeatureSettings = "tnum")
        )

    }

    /**
     * Structures the page. Used for screen titles and main sections.
     */
    object Headline {

        /**
         * - **Usage**: Main title for a screen on a tablet, or the title of a `LargeTopAppBar` (when expanded).
         * - **Size** : 32dp
         * - **Examples**: "My Files", "My Day".
         */
        @Composable
        fun Large(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.headlineLargeEmphasized else MaterialTheme.typography.headlineLarge
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

        /**
         * - **Usage**: The default screen title.
         * - **Size** : 28dp
         * - **Examples**: The title in a large `TopAppBar`.
         */
        @Composable
        fun Medium(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.headlineMediumEmphasized else MaterialTheme.typography.headlineMedium
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

        /**
         * - **Usage**: Section titles, dialog titles.
         * - **Size** : 24dp
         * - **Examples**: The title in a medium `TopAppBar`. "Settings", "Profile".
         */
        @Composable
        fun Small(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.headlineSmallEmphasized else MaterialTheme.typography.headlineSmall
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

    /**
     * Titles for content elements within your UI (cards, lists...).
     */
    object Title {

        /**
         * - **Usage**: The main line in a `ListItem`.
         * - **Size** : 16dp
         * - **Examples**: A contact's name in a list, a song title in a playlist.
         */
        @Composable
        fun Medium(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.titleMedium
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

    /**
     * Readable text, often multi-line. This is the core of your content.
     */
    object Body {

        /**
         * - **Usage**: Secondary text, short descriptions.
         * - **Size** : 14dp
         * - **Examples**: The 2nd line of a `ListItem` (email preview), text for a menu item in a `DropdownMenu`.
         */
        @Composable
        fun Medium(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.bodyMediumEmphasized else MaterialTheme.typography.bodyMedium
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

        /**
         * - **Usage**: Support text, captions.
         * - **Size** : 12dp
         * - **Examples**: Helper text under a `TextField`, legal mentions, 'meta' info (e.g., "3 min ago").
         */
        @Composable
        fun Small(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.bodySmallEmphasized else MaterialTheme.typography.bodySmall
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

    /**
     * Actionable, functional text.
     */
    object Label {

        /**
         * - **Usage**: The default text for buttons.
         * - **Size** : 14dp
         * - **Examples**: Text within a `Button`, `FilledButton`, `TextButton`, `FloatingActionButton`, within a Dialog
         */
        @Composable
        fun Large(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
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

        /**
         * - **Usage**: Smaller functional text.
         * - **Size** : 12dp
         * - **Examples**: Text within a `Chip`, the label for a `NavigationBar` item.
         */
        @Composable
        fun Medium(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.labelMediumEmphasized else MaterialTheme.typography.labelMedium
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

        /**
         * - **Usage**: The smallest functional text.
         * - **Size** : 11dp
         * - **Examples**: Overline text (a small label above a title, e.g., "CATEGORY").
         */
        @Composable
        fun Small(
            text: String?,
            modifier: Modifier = Modifier,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
            lineHeight: TextUnit = TextUnit.Unspecified,
            overflow: TextOverflow = TextOverflow.Clip,
            maxLines: Int = Int.MAX_VALUE,
            minLines: Int = 1,
            onTextLayout : ((TextLayoutResult) -> Unit)? = null
        ) {

            if (text.isNullOrBlank())
                return

            val style = if (emphasized) MaterialTheme.typography.labelSmallEmphasized else MaterialTheme.typography.labelSmall
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

}