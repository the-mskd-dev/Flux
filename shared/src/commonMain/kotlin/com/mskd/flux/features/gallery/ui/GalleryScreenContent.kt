package com.mskd.flux.features.gallery.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.domain.model.thumbnailUrl
import com.mskd.flux.features.gallery.presentation.GalleryFailure
import com.mskd.flux.features.gallery.presentation.GalleryIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.components.FluxTextButton
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.image.Image
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.gallery_empty
import flux.shared.generated.resources.oups_an_error_occured
import flux.shared.generated.resources.retry
import flux.shared.generated.resources.unavailable_check_connection
import org.jetbrains.compose.resources.stringResource

@Composable
fun GalleryScreenContent(
    title: String,
    images: List<GalleryImage>,
    failure: GalleryFailure? = null,
    viewerIndex: Int? = null,
    imageDownloadRunning: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    sendIntent: (GalleryIntent) -> Unit,
) {

    val columns = if (rememberScreenDimensions().isLarge) 3 else 2

    Box(modifier = Modifier.fillMaxSize()) {

        FluxScaffold(
            title = title,
            onBackTap = { sendIntent(GalleryIntent.OnBackTap) }
        ) { innerPadding ->

            if (images.isEmpty() && failure == null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text.Content.Title(
                        text = stringResource(Res.string.gallery_empty),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                return@FluxScaffold
            }

            LazyVerticalStaggeredGrid(
                    modifier = Modifier.fillMaxSize(),
                columns = StaggeredGridCells.Fixed(columns),
                contentPadding = PaddingValues(
                    start = FluxUI.Space.medium,
                    end = FluxUI.Space.medium,
                    top = innerPadding.calculateTopPadding() + FluxUI.Space.small,
                    bottom = innerPadding.calculateBottomPadding() + FluxUI.Space.medium,
                ),
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                verticalItemSpacing = FluxUI.Space.small,
            ) {

                if (failure != null) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text.Content.Body(
                                text = stringResource(
                                    if (failure == GalleryFailure.OFFLINE) Res.string.unavailable_check_connection
                                    else Res.string.oups_an_error_occured
                                ),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            FluxTextButton(
                                text = stringResource(Res.string.retry),
                                onClick = { sendIntent(GalleryIntent.OnRetryTap) }
                            )
                        }
                    }
                }

                itemsIndexed(items = images, key = { _, it -> "${it.type}${it.path}" }) { index, image ->

                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(FluxUI.shapes.corners)
                            .clickable { sendIntent(GalleryIntent.OnImageTap(index)) }
                            .aspectRatio(image.ratio.takeIf { it > 0f } ?: 1f),
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(image.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentScale = ContentScale.Fit,
                        placeholder = Image.placeholder,
                        error = Image.error,
                        contentDescription = null
                    )

                }

            }

        }

        // Drawn over the scaffold; the grid stays composed underneath so it keeps its scroll position.
        // Keyed on visibility only, so a page change doesn't animate and the closing viewer keeps its last index while fading out
        AnimatedContent(
            targetState = viewerIndex,
            modifier = Modifier.fillMaxSize(),
            contentKey = { it != null },
            transitionSpec = { fadeIn() togetherWith fadeOut() using null }
        ) { index ->
            if (index != null) {
                GalleryViewer(
                    images = images,
                    initialIndex = index,
                    imageDownloadRunning = imageDownloadRunning,
                    onPageChange = { sendIntent(GalleryIntent.OnViewerPageChange(it)) },
                    onDownload = { sendIntent(GalleryIntent.OnImageDownloadTap) },
                    onClose = { sendIntent(GalleryIntent.OnViewerClose) }
                )
            }
        }

        // Last child of the Box so that it is drawn above the viewer overlay
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )

    }

}

@FluxPreview
@Composable
fun GalleryScreen_Preview() {
    FluxThemePreview {
        GalleryScreenContent(
            title = "Your name",
            images = listOf(
                GalleryImage(path = "/a.jpg", type = GalleryImageType.POSTER, ratio = 2f / 3f, rating = 7.0),
                GalleryImage(path = "/b.jpg", type = GalleryImageType.BACKDROP, ratio = 16f / 9f, rating = 6.0),
                GalleryImage(path = "/c.jpg", type = GalleryImageType.BACKDROP, ratio = 16f / 9f, rating = 5.0),
            ),
            sendIntent = {}
        )
    }
}
