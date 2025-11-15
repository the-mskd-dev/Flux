package com.kaem.flux.screens.media.composables

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaResumePan(
    overview: MediaOverview,
    media: Media?,
    sendIntent: (MediaIntent) -> Unit,
) {

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(bottom = Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        MediaHeader(
            overview = overview,
            media = media,
            sendIntent = sendIntent
        )

        MediaDescription(media = media)

        if (media is Episode) {
            TextButton(
                onClick = { sendIntent(MediaIntent.OpenEpisodesSheet) },
                content = { Text.Label.Large(text = stringResource(R.string.episode_list)) }
            )
        }

    }

}

@Preview
@Composable
fun MediaResumePan_Preview() {
    FluxTheme {
        MediaResumePan(
            overview = MediaMockups.showOverview,
            media = MediaMockups.episode1,
            sendIntent = {}
        )
    }
}