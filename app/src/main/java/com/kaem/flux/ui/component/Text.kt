package com.kaem.flux.ui.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
object Text {

    /**
     * Very short, high-impact "hero" text. Use for marketing or key moments.
     */
    object Display {

        /**
         * - **Usage**: Main hero text.
         * - **Examples**: Success screens ("Done!"), main metric on a dashboard ("10,000 steps"),
         * clock app home screen ("00:00").
         */
        @Composable
        fun Large(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Secondary headline or catchphrase.
         * - **Examples**: A hero subtitle, an important but secondary metric.
         */
        @Composable
        fun Medium(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Very important page titles on large screens (tablet).
         * - **Examples**: The "Welcome" on a login screen.
         */
        @Composable
        fun Small(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
         * - **Examples**: "My Files", "My Day".
         */
        @Composable
        fun Large(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: The default screen title.
         * - **Examples**: The title in a `CenterAlignedTopAppBar` or `MediumTopAppBar`. "Settings", "Profile".
         */
        @Composable
        fun Medium(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Section titles, dialog titles.
         * - **Examples**: In "Settings", this would be "Notifications" or "Account". Title of an `AlertDialog`.
         */
        @Composable
        fun Small(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
         * - **Examples**: "Recipe of the week", "Your order has arrived".
         */
        @Composable
        fun Large(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: The main line in a `ListItem`.
         * - **Examples**: A contact's name in a list, a song title in a playlist.
         */
        @Composable
        fun Medium(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }


        /**
         * - **Usage**: Sub-section titles or less important elements.
         * - **Examples**: Date headers ("Today"), subtitles within a card.
         */
        @Composable
        fun Small(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
         * - **Examples**: Blog post content, email body, long descriptions.
         */
        @Composable
        fun Large(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Secondary text, short descriptions.
         * - **Examples**: The 2nd line of a `ListItem` (email preview), text for a menu item in a `DropdownMenu`.
         */
        @Composable
        fun Medium(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Support text, captions.
         * - **Examples**: Helper text under a `TextField`, legal mentions, 'meta' info (e.g., "3 min ago").
         */
        @Composable
        fun Small(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
         * - **Examples**: Text within a `Button`, `FilledButton`, `TextButton`, `FloatingActionButton`.
         */
        @Composable
        fun Large(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: Smaller functional text.
         * - **Examples**: Text within a `Chip`, the label for a `NavigationBar` item.
         */
        @Composable
        fun Medium(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

        /**
         * - **Usage**: The smallest functional text.
         * - **Examples**: Overline text (a small label above a title, e.g., "CATEGORY").
         */
        @Composable
        fun Small(
            modifier: Modifier = Modifier,
            text: String?,
            textAlign: TextAlign = TextAlign.Start,
            color: Color = Color.Unspecified,
            emphasized: Boolean = false,
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
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines
            )

        }

    }

}