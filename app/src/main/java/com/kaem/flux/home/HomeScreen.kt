package com.kaem.flux.home

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.utils.Constants

@Composable
fun HomeScreen() {

    val viewModel = viewModel<HomeViewModel>()
    val state by viewModel.uiState.collectAsState()

    Crossfade(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        targetState = state.isLoading,
        label = "HomeScreenAnimation"
    ) {

        when (it) {

            true -> HomeLoading()
            false -> HomeContent(artworks = state.artworks)


        }
        
    }


}

@Composable
fun HomeLoading() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )

    }

}

@Composable
fun HomeContent(artworks: List<FluxArtworkSummary>) {

    if (artworks.isEmpty()) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "No content",
                color = MaterialTheme.colorScheme.primary
            )

            HomePermissionButton()

        }

    } else {

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item(span = { GridItemSpan(3) }) {

                Spacer(
                    modifier = Modifier
                        .systemBarsPadding()
                        .height(20.dp)
                )

            }

            items(artworks) {

                HomeArtwork(artworkSummary = it)

            }

            item(span = { GridItemSpan(3) }) {

                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(20.dp)
                )

            }

        }

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeArtwork(artworkSummary: FluxArtworkSummary) {

    GlideImage(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .aspectRatio(.5f),
        model = Constants.TMDB.IMAGE_SMALL + artworkSummary.imagePath,
        contentDescription = artworkSummary.title,
        loading = placeholder(ColorPainter(Color.LightGray))
    )

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomePermissionButton(viewModel: HomeViewModel = viewModel()) {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        android.Manifest.permission.READ_MEDIA_VIDEO
    else
        android.Manifest.permission.READ_EXTERNAL_STORAGE

    val permissionsState = rememberPermissionState(
        permission = permission,
        onPermissionResult = { result ->

            if (result) {
                viewModel.refreshFiles()
            }

        }
    )

    if (!permissionsState.status.isGranted) {

        FluxButton(
            text = stringResource(id = R.string.give_permission),
            onClick = { permissionsState.launchPermissionRequest() }
        )

    }

}