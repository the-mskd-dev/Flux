package com.mskd.flux.screens.search.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.search.presentation.SearchIntent
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.ui.component.global.FluxBottomSheetItem
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.clear
import flux.shared.generated.resources.genres
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchGenresSheet(
    genres: List<Genre>,
    selectedGenreIds: List<Int>,
    sendIntent: (SearchIntent) -> Unit
) {

    val clearButtonAlpha by animateFloatAsState(if (selectedGenreIds.isEmpty()) 0f else 1f)

    ModalBottomSheet(
        modifier = Modifier.fillMaxWidthWithLimit(),
        contentWindowInsets = { WindowInsets()},
        onDismissRequest = { sendIntent(SearchIntent.ShowGenresSelection(show = false)) },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FluxUI.Space.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text.List.Title(
                    text = stringResource(Res.string.genres)
                )

                TextButton(
                    modifier = Modifier.alpha(clearButtonAlpha),
                    onClick = { if (selectedGenreIds.isNotEmpty()) sendIntent(SearchIntent.ClearGenres) }
                ) {
                    Text.Button.Default(text = stringResource(Res.string.clear))
                }

            }

            genres.forEach { genre ->
                FluxBottomSheetItem(
                    isSelected = selectedGenreIds.contains(genre.id),
                    text = genre.name,
                    onClick = { sendIntent(SearchIntent.SelectGenre(genre = genre)) }
                )
            }

            Spacer(
                Modifier
                    .navigationBarsPadding()
                    .height(FluxUI.Space.large)
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CatalogViewModeSheet_Preview() {
    FluxThemePreview {
        SearchGenresSheet(
            genres = DetailsMockup.allGenres.take(8),
            selectedGenreIds = DetailsMockup.allGenres.take(3).map { it.id },
            sendIntent = {}
        )
    }
}