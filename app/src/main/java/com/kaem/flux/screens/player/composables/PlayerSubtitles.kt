package com.kaem.flux.screens.player.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.text.Cue
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.Ui

@Composable
fun PlayerSubtitles(
    subtitlesState: State<List<Cue>>,
    modifier: Modifier = Modifier
) {

    val subtitles = subtitlesState.value

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.padding(horizontal = Ui.Space.LARGE)
    ) {
        subtitles.forEach {
            SubtitleItem(text = it.text)
        }
    }

}

@Composable
private fun SubtitleItem(text: CharSequence?) {

    text?.let { content ->

        Text.Title.Large(
            modifier = Modifier
                .background(color = Color.Black)
                .padding(horizontal = Ui.Space.MEDIUM, vertical = Ui.Space.SMALL),
            text = content.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
        )

    }

}