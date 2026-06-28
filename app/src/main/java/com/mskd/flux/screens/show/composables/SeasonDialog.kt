package com.mskd.flux.screens.show.composables

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screen.show.ShowIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.no_summary
import flux.shared.generated.resources.season
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonDialog(
    season: Season,
    sendIntent: (ShowIntent) -> Unit
) {

    BasicAlertDialog(
        onDismissRequest = { sendIntent(ShowIntent.CloseDialog) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {

        Card(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .padding(horizontal = FluxUI.Space.medium, vertical = FluxUI.Space.large),
            shape = FluxUI.shapes.corners,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(all = FluxUI.Space.medium),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
            ) {

                Text.Headline.Medium(
                    text = season.title.ifEmpty { stringResource(Res.string.season, season.season) },
                    emphasized = true,
                )

                Text.Body.Large(
                    text = season.description.ifEmpty { stringResource(Res.string.no_summary) },
                )

            }

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