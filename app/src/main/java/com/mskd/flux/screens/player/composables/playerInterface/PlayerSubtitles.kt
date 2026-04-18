package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.text.Cue
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.Ui

@Composable
fun PlayerSubtitles(
    subtitles: () -> List<Cue>,
    smallText: Boolean,
    modifier: Modifier = Modifier
) {

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.padding(horizontal = Ui.Space.LARGE)
    ) {
        subtitles().forEach {
            SubtitleItem(text = it.text, smallText = smallText)
        }
    }

}

@Composable
private fun SubtitleItem(text: CharSequence?, smallText: Boolean) {

    text?.let { content ->

        Text.Adaptive(
            modifier = Modifier
                .clip(shape = Ui.Shape.Corner.ExtraSmall)
                .background(color = Color.Black.copy(.8f))
                .padding(horizontal = Ui.Space.MEDIUM, vertical = Ui.Space.SMALL),
            text = content.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = if (smallText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium
        )

    }

}