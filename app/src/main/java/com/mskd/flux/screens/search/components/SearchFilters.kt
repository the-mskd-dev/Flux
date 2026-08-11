package com.mskd.flux.screens.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.search.presentation.SearchIntent
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.genres
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.shows
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchFilters(
    selectedType: ContentType?,
    selectedGenresCount: Int,
    showGenresFilter: Boolean,
    sendIntent: (SearchIntent) -> Unit
) {

    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    var pendingIntent by rememberSaveable { mutableStateOf<SearchIntent?>(null) }

    LaunchedEffect(pendingIntent) {
        if (pendingIntent == null) return@LaunchedEffect

        snapshotFlow { imeInsets.getBottom(density) }.first { it == 0 }

        pendingIntent?.let { sendIntent(it) }
        pendingIntent = null
    }

    fun prepareIntent(intent: SearchIntent) {
        focusManager.clearFocus(force = true)
        pendingIntent = intent
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        SearchFilterChip(
            text = stringResource(Res.string.movies),
            selected = selectedType == ContentType.MOVIE,
            onClick = { prepareIntent(SearchIntent.FilterOnType(ContentType.MOVIE)) },
        )

        SearchFilterChip(
            text = stringResource(Res.string.shows),
            selected = selectedType == ContentType.SHOW,
            onClick = { prepareIntent(SearchIntent.FilterOnType(ContentType.SHOW)) },
        )

        if (showGenresFilter) {
            SearchFilterChip(
                text = stringResource(Res.string.genres) + if (selectedGenresCount > 0) " ($selectedGenresCount)" else "",
                selected = selectedGenresCount > 0,
                onClick = { prepareIntent(SearchIntent.ShowGenresSelection(show = true)) },
            )
        }

    }

}