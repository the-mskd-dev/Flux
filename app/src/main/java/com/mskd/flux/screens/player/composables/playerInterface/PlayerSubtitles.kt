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
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun PlayerSubtitles(
    subtitles: () -> List<String?>,
    smallText: Boolean,
    modifier: Modifier = Modifier
) {

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.padding(horizontal = FluxUI.Space.large)
    ) {
        subtitles().forEach {
            SubtitleItem(text = it, smallText = smallText)
        }
    }

}

@Composable
private fun SubtitleItem(text: CharSequence?, smallText: Boolean) {

    text?.let { content ->

        Text.Adaptive(
            modifier = Modifier
                .clip(shape = MaterialTheme.shapes.extraSmall)
                .background(color = Color.Black.copy(.8f))
                .padding(horizontal = FluxUI.Space.medium, vertical = FluxUI.Space.small),
            text = content.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = if (smallText) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleMedium
        )

    }

}