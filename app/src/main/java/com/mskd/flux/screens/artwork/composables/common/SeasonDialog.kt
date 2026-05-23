package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDialog(
    season: Season,
    sendIntent: (ArtworkIntent) -> Unit
) {

    BasicAlertDialog(
        onDismissRequest = { sendIntent(ArtworkIntent.ShowPreviewForSeason(null)) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .padding(horizontal = Ui.Space.MEDIUM, vertical = Ui.Space.LARGE),
            shape = Ui.Shape.Corner.Large,
        ) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Ui.Space.MEDIUM, vertical = Ui.Space.LARGE),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                Text.Title.Large(
                    text = season.title.ifEmpty { stringResource(R.string.season, season.season) },
                    emphasized = true,
                )

                Text.Body.Large(
                    text = season.description.ifEmpty { stringResource(R.string.no_summary) },
                )

            }

        }

    }

}

@FluxPreview
@Composable
fun SeasonDialog_Preview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SeasonDialog(
                season = MediaMockups.season1,
                sendIntent = {}
            )
        }
    }
}