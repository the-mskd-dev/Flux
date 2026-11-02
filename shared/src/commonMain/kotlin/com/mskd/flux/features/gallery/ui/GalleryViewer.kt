package com.mskd.flux.features.gallery.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.domain.model.originalUrl
import com.mskd.flux.features.gallery.presentation.ArtworkImageButton
import com.mskd.flux.ui.global.BackGesture
import com.mskd.flux.ui.image.Image
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.gallery_current_backdrop
import flux.shared.generated.resources.gallery_current_poster
import flux.shared.generated.resources.gallery_image_download
import flux.shared.generated.resources.gallery_use_as_backdrop
import flux.shared.generated.resources.gallery_use_as_poster
import flux.shared.generated.resources.ic_download
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val MIN_ZOOM = 1f
private const val DOUBLE_TAP_ZOOM = 3f
private const val MAX_ZOOM = 10f
private const val ZOOM_ANIMATION_MS = 250
private const val DISMISS_ANIMATION_MS = 250

// Scale the image reaches, on top of the swipe down shrink, once the closing animation ends
private const val DISMISS_CLOSED_SCALE = 0.7f

// Downward velocity from which releasing the swipe down dismisses the viewer whatever the distance
private val DISMISS_FLING_VELOCITY = 1_000.dp

// Lower than the platform default (~300 ms) so that a single tap toggles the controls faster
private const val DOUBLE_TAP_TIMEOUT_MS = 200L

/**
 * Full screen overlay showing the images in their original resolution, one page per image.
 * The pager is locked while the current image is zoomed so that a drag pans the image instead.
 * At 1x, a mainly vertical drag down moves the image like a bottom sheet to dismiss the viewer.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
) {

    val pagerState = rememberPagerState(initialPage = initialIndex) { images.size }
    var zoomed by remember { mutableStateOf(false) }
    val currentOnPageChange by rememberUpdatedState(onPageChange)

    var resetRequest by remember { mutableIntStateOf(0) }

    val controls = rememberViewerControlsState()

    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissState = remember(density) {
        ViewerDismissState(flingVelocity = with(density) { DISMISS_FLING_VELOCITY.toPx() })
    }
    var dismissAnimation by remember { mutableStateOf<Job?>(null) }
    var closing by remember { mutableStateOf(false) }
    // From 0 to 1 while the viewer closes: the image fades out and shrinks as it keeps moving down
    var closeProgress by remember { mutableFloatStateOf(0f) }

    fun onDismissStart() {
        dismissAnimation?.cancel()
        controls.hide()
    }

    fun animateDismissTo(target: Float, animationSpec: AnimationSpec<Float>, velocity: Float) {
        dismissAnimation = scope.launch {
            animate(dismissState.offset, target, velocity, animationSpec) { value, _ -> dismissState.snapTo(value) }
        }
    }

    fun onDismissEnd(velocity: Float) {
        when (dismissState.release(velocity)) {
            // The image keeps moving down while the viewer fades out
            ViewerDismissRelease.DISMISS -> {
                closing = true
                onClose()
                animateDismissTo(dismissState.height, tween(DISMISS_ANIMATION_MS), velocity)
                scope.launch {
                    animate(0f, 1f, animationSpec = tween(DISMISS_ANIMATION_MS)) { value, _ -> closeProgress = value }
                }
            }
            ViewerDismissRelease.SETTLE -> animateDismissTo(0f, spring(), velocity)
        }
    }

    // The pager stays locked until the image is back in place, so a settling image can't jump onto another page
    val dismissInProgress by remember(dismissState) { derivedStateOf { dismissState.offset > 0f } }
    LaunchedEffect(imageDownloadRunning) { controls.onDownloadRunningChange(imageDownloadRunning) }

    val baseViewConfiguration = LocalViewConfiguration.current
    val viewerViewConfiguration = remember(baseViewConfiguration) {
        object : ViewConfiguration by baseViewConfiguration {
            override val doubleTapTimeoutMillis: Long = DOUBLE_TAP_TIMEOUT_MS
        }
    }

    // Loading state reported by each page, so the controls only show for a loaded image
    val loadedImages = remember { mutableStateMapOf<String, Boolean>() }
    fun GalleryImage.key() = "$type$path"

    // Back first zooms the image out, then closes the viewer
    BackGesture(onBack = { if (zoomed) resetRequest++ else onClose() })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentOnPageChange(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { dismissState.height = it.height.toFloat() }
            // The Gallery grid shows through as the image is dragged down
            .drawBehind { drawRect(Color.Black.copy(alpha = dismissState.backgroundAlpha)) }
    ) {

        CompositionLocalProvider(LocalViewConfiguration provides viewerViewConfiguration) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !zoomed && !dismissInProgress,
                key = { images.getOrNull(it)?.key() ?: it },
            ) { page ->
                val image = images.getOrNull(page) ?: return@HorizontalPager
                val isCurrent = page == pagerState.currentPage
                ZoomableImage(
                    image = image,
                    isCurrent = isCurrent,
                    resetRequest = resetRequest,
                    dismiss = dismissState.takeIf { isCurrent },
                    dismissible = !closing,
                    closeProgress = { closeProgress },
                    onTap = controls::onTap,
                    onDismissStart = ::onDismissStart,
                    onDismissEnd = ::onDismissEnd,
                    onDismissCancel = { animateDismissTo(0f, spring(), 0f) },
                    onZoomedChange = { zoomed = it },
                    onLoadedChange = { loadedImages[image.key()] = it }
                )
            }
        }

        val currentImage = images.getOrNull(pagerState.currentPage)
        val currentLoaded = currentImage?.let { loadedImages[it.key()] } == true
        ViewerControl(visible = controls.visible && currentImage != null && currentLoaded) {
            // Still needed for the smart cast, and keeps the content empty if the image vanishes mid fade-out
            if (currentImage == null) return@ViewerControl
            val onDownloadClick = {
                controls.onButtonInteraction()
                onDownload()
            }
            if (artworkImageButton == ArtworkImageButton.HIDDEN) {
                ImageDownloadButton(
                    imageDownloadRunning = imageDownloadRunning,
                    onClick = onDownloadClick,
                )
            } else {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.TonalLeadingButton(
                            colors = viewerButtonColors(),
                            enabled = artworkImageButton == ArtworkImageButton.AVAILABLE,
                            onClick = {
                                controls.onButtonInteraction()
                                onUseImage()
                            }
                        ) {
                            Text(text = stringResource(useImageLabel(currentImage.type, artworkImageButton)))
                        }
                    },
                    trailingButton = {
                        val downloadDescription = stringResource(Res.string.gallery_image_download)
                        SplitButtonDefaults.TrailingButton(
                            modifier = Modifier.semantics { contentDescription = downloadDescription },
                            enabled = !imageDownloadRunning,
                            colors = viewerButtonColors(),
                            elevation = ButtonDefaults.filledTonalButtonElevation(),
                            onClick = onDownloadClick,
                        ) {
                            ImageDownloadIcon(imageDownloadRunning = imageDownloadRunning)
                        }
                    },
                )
            }
        }
    }
}

/** Tonal colours whose disabled state stays readable on the black viewer background, whatever the theme. */
@Composable
private fun viewerButtonColors() = ButtonDefaults.filledTonalButtonColors(
    disabledContainerColor = DISABLED_CONTAINER_COLOR,
    disabledContentColor = DISABLED_CONTENT_COLOR,
)

private val DISABLED_CONTAINER_COLOR = Color.White.copy(alpha = .12f)
private val DISABLED_CONTENT_COLOR = Color.White.copy(alpha = .6f)

private fun useImageLabel(type: GalleryImageType, state: ArtworkImageButton): StringResource {
    val current = state == ArtworkImageButton.CURRENT
    return when (type) {
        GalleryImageType.POSTER -> if (current) Res.string.gallery_current_poster else Res.string.gallery_use_as_poster
        GalleryImageType.BACKDROP -> if (current) Res.string.gallery_current_backdrop else Res.string.gallery_use_as_backdrop
    }
}

/** Standalone Image Download button, shown in place of the split button when Use as Poster/Backdrop is hidden. */
@Composable
private fun ImageDownloadButton(imageDownloadRunning: Boolean, onClick: () -> Unit) {
    val downloadDescription = stringResource(Res.string.gallery_image_download)
    FilledTonalIconButton(
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            disabledContainerColor = DISABLED_CONTAINER_COLOR,
            disabledContentColor = DISABLED_CONTENT_COLOR,
        ),
        modifier = Modifier.semantics { contentDescription = downloadDescription },
        enabled = !imageDownloadRunning,
        onClick = onClick,
    ) {
        ImageDownloadIcon(imageDownloadRunning = imageDownloadRunning)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImageDownloadIcon(imageDownloadRunning: Boolean) {
    if (imageDownloadRunning) {
        LoadingIndicator(modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize))
    } else {
        Icon(
            modifier = Modifier.size(SplitButtonDefaults.TrailingIconSize),
            painter = painterResource(Res.drawable.ic_download),
            contentDescription = null,
        )
    }
}

/** The viewer controls, centred at the bottom of the screen above the navigation bar, fading in and out with [visible]. */
@Composable
private fun BoxScope.ViewerControl(
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ZoomableImage(
    image: GalleryImage,
    isCurrent: Boolean,
    resetRequest: Int,
    dismiss: ViewerDismissState?,
    dismissible: Boolean,
    onTap: () -> Unit,
    onDismissStart: () -> Unit,
    onDismissEnd: (velocity: Float) -> Unit,
    onDismissCancel: () -> Unit,
    closeProgress: () -> Float,
    onZoomedChange: (Boolean) -> Unit,
    onLoadedChange: (Boolean) -> Unit,
) {

    var scale by remember { mutableFloatStateOf(MIN_ZOOM) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var loading by remember { mutableStateOf(true) }
    var animation by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val currentOnTap by rememberUpdatedState(onTap)
    val currentDismiss by rememberUpdatedState(dismiss)
    val currentDismissible by rememberUpdatedState(dismissible)
    val currentOnDismissStart by rememberUpdatedState(onDismissStart)
    val currentOnDismissEnd by rememberUpdatedState(onDismissEnd)
    val currentOnDismissCancel by rememberUpdatedState(onDismissCancel)

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
            // The tap gesture alone is invisible to accessibility services, which would leave the controls unreachable
            .semantics { onClick { currentOnTap(); true } }
            .pointerInput(Unit) {
                // A double-tap zooms only: onTap is delayed by the double-tap timeout and never fires for it
                detectTapGestures(
                    onTap = { currentOnTap() },
                    onDoubleTap = { tap ->
                        val focus = tap - Offset(size.width / 2f, size.height / 2f)
                        if (scale > MIN_ZOOM) animateZoomTo(MIN_ZOOM, focus)
                        else animateZoomTo(DOUBLE_TAP_ZOOM, focus)
                    }
                )
            }
            .pointerInput(Unit) {
                // Only consumes pinches, drags once zoomed and the swipe down to dismiss, so a horizontal 1x drag
                // still reaches the pager. Past the touch slop, a 1x drag mainly downward becomes the swipe down.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val velocityTracker = VelocityTracker()
                    var drag = Offset.Zero
                    var gestureClaimed = false
                    var dismissing = false
                    try {
                        do {
                            val event = awaitPointerEvent()
                            val multiTouch = event.changes.count { it.pressed } > 1
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (dismissing) {
                                if (change != null) {
                                    velocityTracker.addPointerInputChange(change)
                                    currentDismiss?.drag(change.positionChange().y)
                                    change.consume()
                                }
                            } else if (multiTouch || scale > MIN_ZOOM) {
                                gestureClaimed = true
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                if (zoom != 1f || pan != Offset.Zero) {
                                    animation?.cancel()
                                    val centroid = event.calculateCentroid(useCurrent = false)
                                    val focus = centroid - Offset(size.width / 2f, size.height / 2f)
                                    transform(scale * zoom, focus, pan)
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } else if (!gestureClaimed && change != null && currentDismissible && currentDismiss != null) {
                                velocityTracker.addPointerInputChange(change)
                                drag += change.positionChange()
                                if (drag.getDistance() > viewConfiguration.touchSlop) {
                                    gestureClaimed = true
                                    if (drag.y > abs(drag.x)) {
                                        dismissing = true
                                        currentOnDismissStart()
                                        currentDismiss?.drag(change.positionChange().y)
                                        change.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                        if (dismissing) currentOnDismissEnd(velocityTracker.calculateVelocity().y)
                    } catch (e: CancellationException) {
                        // A page leaving mid-drag settles the image back rather than leaving it off its resting position
                        if (dismissing) currentOnDismissCancel()
                        throw e
                    }
                }
            }
            .graphicsLayer {
                // The swipe down only happens at 1x, where the zoom offset is zero
                // Read in the draw phase only, so the closing animation doesn't recompose the page
                val close = closeProgress()
                val dismissScale = (dismiss?.scale ?: 1f) * (1f - (1f - DISMISS_CLOSED_SCALE) * close)
                alpha = 1f - close
                scaleX = scale * dismissScale
                scaleY = scale * dismissScale
                translationX = offset.x
                translationY = offset.y + (dismiss?.offset ?: 0f)
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
