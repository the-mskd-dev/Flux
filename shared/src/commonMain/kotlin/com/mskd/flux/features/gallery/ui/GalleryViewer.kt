package com.mskd.flux.features.gallery.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.originalUrl
import com.mskd.flux.ui.global.BackGesture
import com.mskd.flux.ui.image.Image
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val MIN_ZOOM = 1f
private const val DOUBLE_TAP_ZOOM = 3f
private const val MAX_ZOOM = 10f
private const val ZOOM_ANIMATION_MS = 250

/**
 * Full screen overlay showing the images in their original resolution, one page per image.
 * The pager is locked while the current image is zoomed so that a drag pans the image instead.
 */
@Composable
fun GalleryViewer(
    images: List<GalleryImage>,
    initialIndex: Int,
    onPageChange: (Int) -> Unit,
    onClose: () -> Unit,
) {

    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    var zoomed by remember { mutableStateOf(false) }
    val currentOnPageChange by rememberUpdatedState(onPageChange)

    var resetRequest by remember { mutableIntStateOf(0) }

    // Back first zooms the image out, then closes the viewer
    BackGesture(onBack = { if (zoomed) resetRequest++ else onClose() })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentOnPageChange(it) }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = !zoomed,
            key = { images.getOrNull(it)?.let { image -> "${image.type}${image.path}" } ?: it },
        ) { page ->
            val image = images.getOrNull(page) ?: return@HorizontalPager
            ZoomableImage(
                image = image,
                isCurrent = page == pagerState.currentPage,
                resetRequest = resetRequest,
                onZoomedChange = { zoomed = it }
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding(),
            onClick = onClose
        ) {
            Icon(
                imageVector = Icons.Rounded.Clear,
                tint = Color.White,
                contentDescription = "close button",
            )
        }

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ZoomableImage(
    image: GalleryImage,
    isCurrent: Boolean,
    resetRequest: Int,
    onZoomedChange: (Boolean) -> Unit,
) {

    var scale by remember { mutableFloatStateOf(MIN_ZOOM) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var loading by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    // Keeps the zoomed image from being dragged out of the screen
    fun clamp(offset: Offset, scale: Float): Offset {
        val maxX = size.width * (scale - 1f) / 2f
        val maxY = size.height * (scale - 1f) / 2f
        return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }

    // Zooms to newScale keeping the content under focus (relative to the center) fixed on screen, then pans
    fun transform(newScale: Float, focus: Offset, pan: Offset = Offset.Zero) {
        val target = newScale.coerceIn(MIN_ZOOM, MAX_ZOOM)
        scale.let { current ->
            offset = clamp(focus - (focus - offset) * (target / current) + pan, target)
        }
        scale = target
    }

    fun animateZoomTo(target: Float, focus: Offset) {
        animation?.cancel()
        val startScale = scale
        val startOffset = offset
        val endOffset = clamp(focus - (focus - startOffset) * (target / startScale), target)
        animation = scope.launch {
            animate(0f, 1f, animationSpec = tween(ZOOM_ANIMATION_MS)) { progress, _ ->
                scale = startScale + (target - startScale) * progress
                offset = startOffset + (endOffset - startOffset) * progress
            }
        }
    }

    // Only the displayed page drives the pager lock, a page swiped away goes back to 1x
    LaunchedEffect(isCurrent) {
        if (!isCurrent) {
            animation?.cancel()
            scale = MIN_ZOOM
            offset = Offset.Zero
        }
    }
    LaunchedEffect(resetRequest) {
        if (isCurrent && resetRequest > 0) animateZoomTo(MIN_ZOOM, Offset.Zero)
    }
    val isZoomed = scale > MIN_ZOOM
    LaunchedEffect(isCurrent, isZoomed) {
        if (isCurrent) onZoomedChange(isZoomed)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tap ->
                        val focus = tap - Offset(size.width / 2f, size.height / 2f)
                        if (scale > MIN_ZOOM) animateZoomTo(MIN_ZOOM, focus)
                        else animateZoomTo(DOUBLE_TAP_ZOOM, focus)
                    }
                )
            }
            .pointerInput(Unit) {
                // Only consumes pinches, and drags once zoomed, so a 1x drag still reaches the pager
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val multiTouch = event.changes.count { it.pressed } > 1
                        if (multiTouch || scale > MIN_ZOOM) {
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            if (zoom != 1f || pan != Offset.Zero) {
                                animation?.cancel()
                                val centroid = event.calculateCentroid(useCurrent = false)
                                val focus = centroid - Offset(size.width / 2f, size.height / 2f)
                                transform(scale * zoom, focus, pan)
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
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
            onLoading = { loading = true },
            onSuccess = { loading = false },
            onError = { loading = false },
            contentDescription = null
        )
        if (loading) {
            LoadingIndicator()
        }
    }

}
