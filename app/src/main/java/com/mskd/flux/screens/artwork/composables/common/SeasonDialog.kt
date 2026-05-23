package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@Composable
fun SeasonDialog(
    season: Season,
    sendIntent: (ArtworkIntent) -> Unit
) {

    Dialog(
        onDismissRequest = { sendIntent(ArtworkIntent.ShowPreviewForSeason(null)) }
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            Text.Title.Large(
                text = season.title
            )

            Text.Body.Large(
                text = season.description
            )

        }

    }

}

@FluxPreview
@Composable
fun SeasonDialog_Preview() {
    AppTheme {
        SeasonDialog(
            season = MediaMockups.season1,
            sendIntent = {}
        )
    }
}