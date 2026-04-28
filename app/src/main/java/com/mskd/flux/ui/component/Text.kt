package com.mskd.flux.ui.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

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
    ) {

        if (text.isNullOrBlank())
            return

        Text(
            modifier = modifier,
            text = text,
            color = color,
            style = style,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines
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

        Text(
            modifier = modifier,
            text = text,
            color = color,
            style = style,
            textAlign = textAlign,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines
        )

    }

    /**
     * Very short, high-impact "hero" text. Use for marketing or key moments.
     */
    object Display {

        /**
         * - **Usage**: Main hero text.
         * - **Size** : 57dp
         * - **Examples**: Success screens ("Done!"), main metric on a dashboard ("10,000 steps"),
         * clock app home screen ("00:00").
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.displayLargeEmphasized else MaterialTheme.typography.displayLarge,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Secondary headline or catchphrase.
         * - **Size** : 45dp
         * - **Examples**: A hero subtitle, an important but secondary metric.
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.displayMediumEmphasized else MaterialTheme.typography.displayMedium,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Very important page titles on large screens (tablet).
         * - **Size** : 36dp
         * - **Examples**: The "Welcome" on a login screen.
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.displaySmallEmphasized else MaterialTheme.typography.displaySmall,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.headlineLargeEmphasized else MaterialTheme.typography.headlineLarge,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: The default screen title.
         * - **Size** : 28dp
         * - **Examples**: The title in a `CenterAlignedTopAppBar` or `MediumTopAppBar`. "Settings", "Profile".
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.headlineMediumEmphasized else MaterialTheme.typography.headlineMedium,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Section titles, dialog titles.
         * - **Size** : 24dp
         * - **Examples**: In "Settings", this would be "Notifications" or "Account". Title of an `AlertDialog`.
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.headlineSmallEmphasized else MaterialTheme.typography.headlineSmall,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

    }

    /**
     * Titles for content elements within your UI (cards, lists...).
     */
    object Title {

        /**
         * - **Usage**: Title within a `Card`, subject of an email (in detail view).
         * - **Size** : 22dp
         * - **Examples**: "Recipe of the week", "Your order has arrived".
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.titleLargeEmphasized else MaterialTheme.typography.titleLarge,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.titleMedium,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }


        /**
         * - **Usage**: Sub-section titles or less important elements.
         * - **Size** : 14dp
         * - **Examples**: Date headers ("Today"), subtitles within a card.
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.titleSmallEmphasized else MaterialTheme.typography.titleSmall,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

    }

    /**
     * Readable text, often multi-line. This is the core of your content.
     */
    object Body {

        /**
         * - **Usage**: The main body text.
         * - **Size** : 16dp
         * - **Examples**: Blog post content, email body, long descriptions.
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.bodyLargeEmphasized else MaterialTheme.typography.bodyLarge,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.bodyMediumEmphasized else MaterialTheme.typography.bodyMedium,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.bodySmallEmphasized else MaterialTheme.typography.bodySmall,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.labelMediumEmphasized else MaterialTheme.typography.labelMedium,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
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
            minLines: Int = 1
        ) {

            if (text.isNullOrBlank())
                return

            Text(
                modifier = modifier,
                text = text,
                color = color,
                style = if (emphasized) MaterialTheme.typography.labelSmallEmphasized else MaterialTheme.typography.labelSmall,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

    }

}