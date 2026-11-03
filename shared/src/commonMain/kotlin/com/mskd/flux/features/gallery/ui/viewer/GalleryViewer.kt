package com.mskd.flux.features.gallery.ui.viewer

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.presentation.ArtworkImageButton
import com.mskd.flux.ui.global.BackGesture
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val DISMISS_ANIMATION_MS = 250

// Downward velocity from which releasing the swipe down dismisses the viewer whatever the distance
private val DISMISS_FLING_VELOCITY = 1_000.dp

// Lower than the platform default (~300 ms) so that a single tap toggles the controls faster
private const val DOUBLE_TAP_TIMEOUT_MS = 200L

/**
 * Full screen overlay showing the images in their original resolution, one page per image.
 * The pager is locked while the current image is zoomed so that a drag pans the image instead.
 * At 1x, a mainly vertical drag down moves the image like a bottom sheet to dismiss the viewer. Dismissed by the swipe
 * down or by Back, the image flies back to its thumbnail, found in [thumbnailBounds] (in root coordinates) and rounded
 * like [thumbnailShape], then [onReturnedToThumbnail] closes the viewer. Without a thumbnail on screen, the image
 * slides down and [onClose] closes the viewer.
 */
@Composable
fun GalleryViewer(
    images: List<GalleryImage>,
    initialIndex: Int,
    imageDownloadRunning: Boolean,
    artworkImageButton: ArtworkImageButton,
    onPageChange: (Int) -> Unit,
    onDownload: () -> Unit,
    onUseImage: () -> Unit,
    onClose: () -> Unit,
    onReturnedToThumbnail: () -> Unit,
    thumbnailShape: CornerBasedShape,
    thumbnailBounds: (GalleryImage) -> Rect?,
) {

    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    val currentOnPageChange by rememberUpdatedState(onPageChange)
    var zoomed by remember { mutableStateOf(false) }
    var zoomOutRequest by remember { mutableIntStateOf(0) }
    val controls = rememberViewerControlsState()

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissState = remember(density) {
        ViewerDismissState(flingVelocity = with(density) { DISMISS_FLING_VELOCITY.toPx() })
    }
    var dismissAnimation by remember { mutableStateOf<Job?>(null) }
    var closing by remember { mutableStateOf(false) }
    var returnFlight by remember { mutableStateOf<ViewerReturnFlight?>(null) }
    // Position of the viewer in the root, to bring the thumbnail bounds into the viewer coordinates
    var viewerBounds by remember { mutableStateOf(Rect.Zero) }

    // Loading state reported by each page, so the controls only show for a loaded image
    val loadedImages = remember { mutableStateMapOf<String, Boolean>() }
    fun GalleryImage.key() = "$type$path"

    fun onSwipeDownStart() {
        dismissAnimation?.cancel()
        controls.hide()
    }

    fun settleBack(velocity: Float = 0f) {
        dismissAnimation = scope.launch { dismissState.animateTo(0f, spring(), velocity) }
    }

    fun dismiss(velocity: Float = 0f) {
        if (closing) return
        closing = true
        dismissAnimation?.cancel()
        controls.hide()
        val image = images.getOrNull(pagerState.currentPage)
        val flight = image?.let {
            returnFlightOrNull(viewerBounds, it.ratio, dismissState, thumbnailBounds(it), thumbnailShape, density)
        }
        if (flight != null) {
            returnFlight = flight
            scope.launch {
                flight.fly()
                onReturnedToThumbnail()
            }
        } else {
            // Without a thumbnail to go back to, the image keeps moving down while the viewer fades out
            onClose()
            scope.launch { dismissState.animateTo(dismissState.height, tween(DISMISS_ANIMATION_MS), velocity) }
        }
    }

    fun onSwipeDownEnd(velocity: Float) {
        when (dismissState.release(velocity)) {
            ViewerDismissRelease.DISMISS -> dismiss(velocity)
            ViewerDismissRelease.SETTLE -> settleBack(velocity)
        }
    }

    // The pager stays locked until the image is back in place, so a settling image can't jump onto another page
    val dismissInProgress by remember(dismissState) { derivedStateOf { dismissState.offset > 0f } }

    val baseViewConfiguration = LocalViewConfiguration.current
    val viewerViewConfiguration = remember(baseViewConfiguration) {
        object : ViewConfiguration by baseViewConfiguration {
            override val doubleTapTimeoutMillis: Long = DOUBLE_TAP_TIMEOUT_MS
        }
    }

    LaunchedEffect(imageDownloadRunning) { controls.onDownloadRunningChange(imageDownloadRunning) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentOnPageChange(it) }
    }

    // Back first zooms the image out, then dismisses the viewer like the swipe down
    BackGesture(onBack = { if (zoomed) zoomOutRequest++ else dismiss() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                viewerBounds = Rect(it.positionInRoot(), it.size.toSize())
                dismissState.height = it.size.height.toFloat()
            }
            // The Gallery grid shows through as the image is dragged down, then fully as it flies back to its thumbnail
            .drawBehind {
                val flightProgress = returnFlight?.progress ?: 0f
                drawRect(Color.Black.copy(alpha = dismissState.backgroundAlpha * (1f - flightProgress)))
            }
    ) {

        CompositionLocalProvider(LocalViewConfiguration provides viewerViewConfiguration) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !zoomed && !dismissInProgress && !closing,
                key = { images.getOrNull(it)?.key() ?: it },
            ) { page ->
                val image = images.getOrNull(page) ?: return@HorizontalPager
                val isCurrent = page == pagerState.currentPage
                ZoomableImage(
                    image = image,
                    isCurrent = isCurrent,
                    zoomOutRequest = zoomOutRequest,
                    dismissState = dismissState.takeIf { isCurrent },
                    dismissible = !closing,
                    returnFlight = returnFlight.takeIf { isCurrent },
                    onTap = controls::onTap,
                    onSwipeDownStart = ::onSwipeDownStart,
                    onSwipeDownEnd = ::onSwipeDownEnd,
                    onSwipeDownCancel = { settleBack() },
                    onZoomedChange = { zoomed = it },
                    onLoadedChange = { loadedImages[image.key()] = it }
                )
            }
        }

        val currentImage = images.getOrNull(pagerState.currentPage)
        ViewerControls(
            visible = controls.visible && currentImage?.let { loadedImages[it.key()] } == true,
            imageType = currentImage?.type,
            artworkImageButton = artworkImageButton,
            imageDownloadRunning = imageDownloadRunning,
            onUseImage = {
                controls.onButtonInteraction()
                onUseImage()
            },
            onDownload = {
                controls.onButtonInteraction()
                onDownload()
            },
        )

    }
}
