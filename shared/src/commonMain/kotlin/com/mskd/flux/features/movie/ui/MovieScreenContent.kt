package com.mskd.flux.features.movie.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mskd.flux.features.artwork.ui.composables.ArtworkDialogs
import com.mskd.flux.features.movie.presentation.MovieContent
import com.mskd.flux.features.movie.presentation.MovieIntent
import com.mskd.flux.features.movie.presentation.MoviePageDialog
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.theme.FluxTheme

@Composable
fun MovieScreenContent(
    content: MovieContent,
    sendIntent: (MovieIntent) -> Unit,
) {

    val isLargeScreen = rememberScreenDimensions().isLarge
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showMenu by remember { mutableStateOf(false) }

    FluxScaffold(
        // The title is shown by the page itself
        title = null,
        animatedTitle = true,
        actions = {
            IconButton(
                onClick = { showMenu = true },
                content = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "menu button"
                    )
                }
            )

            MovieDropDownMenu(
                isVisible = showMenu,
                fullMovie = content.fullMovie,
                identification = content.identification,
                onDismissRequest = { showMenu = false },
                sendIntent = sendIntent
            )
        },
        onBackTap = { sendIntent(MovieIntent.OnBackTap) },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->

        if (isLargeScreen) {
            MovieContentLarge(content = content, scaffoldInnerPadding = innerPadding, sendIntent = sendIntent)
        } else {
            MovieContentRegular(content = content, scaffoldInnerPadding = innerPadding, sendIntent = sendIntent)
        }

    }

    ArtworkDialogs(
        artworkId = content.fullMovie.artwork.id,
        isIdentificationVisible = content.dialog is MoviePageDialog.Identification,
        isResetProgressVisible = content.dialog is MoviePageDialog.ResetProgressConfirmation,
        onDismiss = { sendIntent(MovieIntent.CloseDialog) },
        onIdentified = { artworkId, type -> sendIntent(MovieIntent.OnIdentified(artworkId = artworkId, type = type)) },
        onResetProgress = { sendIntent(MovieIntent.ResetProgress) },
    )

}

@FluxPreview
@Composable
fun MovieScreenContent_Preview() {
    FluxTheme {
        MovieScreenContent(content = previewMovieContent, sendIntent = {})
    }
}
