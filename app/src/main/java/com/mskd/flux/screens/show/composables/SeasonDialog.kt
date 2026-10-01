package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.features.show.presentation.ShowIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.presentation.FluxUI
import com.mskd.flux.presentation.text.Text
import com.mskd.flux.presentation.modal.dialog.FluxDialog
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.close
import flux.shared.generated.resources.no_summary
import flux.shared.generated.resources.season
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDialog(
    season: Season,
    sendIntent: (ShowIntent) -> Unit
) {

    FluxDialog(
        onDismiss = { sendIntent(ShowIntent.CloseDialog) },
        onDismissLabel = stringResource(Res.string.close),
        title = season.title.ifEmpty { stringResource(Res.string.season, season.season) }
    ) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(all = FluxUI.Space.medium),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {

            Text.Content.Body(
                text = season.description.ifEmpty { stringResource(Res.string.no_summary) },
            )

        }

    }

}

@FluxPreview
@Composable
fun SeasonDialog_Preview() {
    FluxTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SeasonDialog(
                season = MediaMockups.season1,
                sendIntent = {}
            )
        }
    }
}