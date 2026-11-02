package com.mskd.flux.features.gallery.ui.viewer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.originalUrl
import com.mskd.flux.ui.image.Image

/**
 * A page of the Gallery viewer: the image in its original resolution, zoomable, and draggable down to dismiss the
 * viewer when [dismissible]. [dismissState] and [returnFlight] are only given to the displayed page.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ZoomableImage(
    image: GalleryImage,
    isCurrent: Boolean,
    zoomOutRequest: Int,
    dismissState: ViewerDismissState?,
    dismissible: Boolean,
    returnFlight: ViewerReturnFlight?,
    onTap: () -> Unit,
    onSwipeDownStart: () -> Unit,
    onSwipeDownEnd: (velocity: Float) -> Unit,
    onSwipeDownCancel: () -> Unit,
    onZoomedChange: (Boolean) -> Unit,
    onLoadedChange: (Boolean) -> Unit,
) {

    val zoom = rememberZoomState()
    var loading by remember { mutableStateOf(true) }
    val currentOnTap by rememberUpdatedState(onTap)
    val currentDismissState by rememberUpdatedState(dismissState)
    val currentDismissible by rememberUpdatedState(dismissible)
    val currentOnSwipeDownStart by rememberUpdatedState(onSwipeDownStart)
    val currentOnSwipeDownEnd by rememberUpdatedState(onSwipeDownEnd)
    val currentOnSwipeDownCancel by rememberUpdatedState(onSwipeDownCancel)

    // Only the displayed page drives the pager lock, a page swiped away goes back to 1x
    LaunchedEffect(isCurrent) {
        if (!isCurrent) zoom.reset()
    }
    LaunchedEffect(zoomOutRequest) {
        if (isCurrent && zoomOutRequest > 0) zoom.animateZoomOut()
    }
    val isZoomed = zoom.isZoomed
    LaunchedEffect(isCurrent, isZoomed) {
        if (isCurrent) onZoomedChange(isZoomed)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { zoom.pageSize = it }
            // The tap gesture alone is invisible to accessibility services, which would leave the controls unreachable
            .semantics { onClick { currentOnTap(); true } }
            .pointerInput(Unit) {
                // A double-tap zooms only: onTap is delayed by the double-tap timeout and never fires for it
                detectTapGestures(
                    onTap = { currentOnTap() },
                    onDoubleTap = { zoom.toggleZoom(it) }
                )
            }
            .pointerInput(Unit) {
                detectZoomAndSwipeDown(
                    zoom = zoom,
                    dismissState = { currentDismissState.takeIf { currentDismissible } },
                    onSwipeDownStart = { currentOnSwipeDownStart() },
                    onSwipeDownEnd = { currentOnSwipeDownEnd(it) },
                    onSwipeDownCancel = { currentOnSwipeDownCancel() },
                )
            }
            .graphicsLayer {
                if (returnFlight != null) {
                    with(returnFlight) { applyTo(pageSize = size, ratio = image.ratio) }
                    return@graphicsLayer
                }
                // The swipe down only happens at 1x, where the zoom offset is zero
                val dismissScale = dismissState?.scale ?: 1f
                scaleX = zoom.scale * dismissScale
                scaleY = zoom.scale * dismissScale
                translationX = zoom.offset.x
                translationY = zoom.offset.y + (dismissState?.offset ?: 0f)
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(image.originalUrl)
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            error = Image.error,
            onLoading = { loading = true; onLoadedChange(false) },
            onSuccess = { loading = false; onLoadedChange(true) },
            onError = { loading = false; onLoadedChange(false) },
            contentDescription = null
        )
        if (loading) {
            LoadingIndicator()
        }
    }

}
