package com.mskd.flux.screens.player.composables.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.mockups.PlayerMockups
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.formatMinSec
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.chapter_number
import flux.shared.generated.resources.chapters
import org.jetbrains.compose.resources.stringResource

@Composable
fun PlayerChaptersSheet(
    chapters: List<VideoChapter>,
    currentChapter: VideoChapter?,
    sendIntent: (PlayerIntent) -> Unit
) {

    BasicAlertDialog(
        modifier = Modifier
            .clip(AlertDialogDefaults.shape),
        onDismissRequest = { sendIntent(PlayerIntent.ShowSettings(sheet = null)) },
    ) {

        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

            LazyColumn(modifier = Modifier.fillMaxWidth()) {

                stickyHeader {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {

                        Text.Content.Title(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = FluxUI.Space.large, start = FluxUI.Space.medium, end = FluxUI.Space.medium, bottom = FluxUI.Space.medium),
                            text = stringResource(Res.string.chapters),
                        )

                        HorizontalDivider(modifier = Modifier.fillMaxWidth())

                    }

                }

                itemsIndexed(chapters) { index, chapter ->

                    if (index != 0)
                        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = FluxUI.Space.medium))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sendIntent(PlayerIntent.SelectChapter(chapter = chapter)) }
                            .padding(horizontal = FluxUI.Space.medium, vertical = FluxUI.Space.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                    ) {

                        Text.List.Title(
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            text = chapterTitle(chapter = chapter, chapters = chapters),
                        )

                        Text.List.Body(text = chapter.start.formatMinSec())

                        if (chapter == currentChapter) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "current chapter"
                            )
                        }

                    }

                }
            }

        }

    }

}

/** Title of [chapter], or "Chapter N" (N being its 1-based position) when it has none. */
@Composable
fun chapterTitle(chapter: VideoChapter, chapters: List<VideoChapter>): String =
    chapter.title ?: stringResource(Res.string.chapter_number, chapters.indexOf(chapter) + 1)

@FluxPreview
@Composable
fun PlayerChaptersSheet_Preview() {
    FluxTheme {
        PlayerChaptersSheet(
            chapters = PlayerMockups.chapters,
            currentChapter = PlayerMockups.chapters[1],
            sendIntent = {}
        )
    }
}
