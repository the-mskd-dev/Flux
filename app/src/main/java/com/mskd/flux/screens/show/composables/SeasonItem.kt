package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.palette.graphics.Palette
import coil3.toBitmap
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.ui.component.Image
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.AppThemePreview
import com.mskd.flux.utils.extensions.tmdbImage
import kotlin.text.ifEmpty

@Composable
fun SeasonItem(
    modifier: Modifier = Modifier,
    season: Season,
    onTap: (Int?) -> Unit,
    onLongPress: () -> Unit
) {

    val url = season.imagePath.orEmpty().tmdbImage
    var seedRgb by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .combinedClickable(
                onClick = { onTap(seedRgb) },
                onLongClick = { onLongPress() }
            ),
    ) {

        Image(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .fillMaxWidth()
                .aspectRatio(3f/4f),
            url = url,
            contentDescription = season.title,
            onSuccess = { state ->
                val bitmap = state.result.image.toBitmap()
                Palette.from(bitmap).generate { palette ->
                    seedRgb = palette?.dominantSwatch?.rgb
                }
            }
        )

        Text.Title.Large(
            modifier = Modifier
                .padding(top = Ui.Space.LARGE)
                .padding(horizontal = Ui.Space.MEDIUM),
            text = season.title.ifEmpty { stringResource(R.string.season, season.season) },
            color = MaterialTheme.colorScheme.onSurface,
            emphasized = true
        )

        Spacer(modifier = Modifier.height(Ui.Space.LARGE))

    }

}

@Preview
@Composable
fun SeasonItem_Preview() {
    AppThemePreview {
        SeasonItem(
            season = MediaMockups.season1,
            onTap = {},
            onLongPress = {}
        )
    }
}