package com.mskd.flux.features.artwork.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.bleedHorizontal
import com.mskd.flux.ui.theme.FluxUI

/**
 * Header of an Artwork page in the regular layout: the Artwork image across the whole screen width,
 * the Artwork title over the bottom of the image, and the page [buttons] below.
 *
 * @param allowBlurredHeader whether the image may use the blurred header, when the user chose it.
 * @param titleHorizontalMargin margin between the title and the screen edges.
 */
@Composable
fun ArtworkPageHeader(
    modifier: Modifier = Modifier,
    fullArtwork: FullArtwork,
    currentMedia: Media? = null,
    selectedSeason: Int? = null,
    showTitle: Boolean,
    allowBlurredHeader: Boolean,
    isImageBlurred: Boolean = false,
    titleHorizontalMargin: Dp = FluxUI.Space.medium,
    onImageClick: () -> Unit,
    buttons: (@Composable (Modifier) -> Unit)? = null,
) {

    ConstraintLayout(
        modifier = modifier
            .bleedHorizontal()
            .fillMaxWidth()
    ) {

        val (image, text, buttonsRef) = createRefs()

        ArtworkImage(
            modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .aspectRatio(FluxUI.Ratio.rectangle),
            fullArtwork = fullArtwork,
            currentMedia = currentMedia,
            selectedSeason = selectedSeason,
            isBlurred = isImageBlurred,
            allowBlurredHeader = allowBlurredHeader,
            onClick = onImageClick
        )

        if (showTitle) {
            ArtworkTitle(
                modifier = Modifier.constrainAs(text) {
                    top.linkTo(image.bottom)
                    start.linkTo(parent.start, titleHorizontalMargin)
                    end.linkTo(parent.end, titleHorizontalMargin)
                    bottom.linkTo(image.bottom)
                    width = Dimension.preferredWrapContent
                },
                artwork = fullArtwork.artwork,
            )
        }

        buttons?.invoke(
            Modifier.constrainAs(buttonsRef) {
                if (showTitle) top.linkTo(text.bottom, FluxUI.Space.large)
                else top.linkTo(image.bottom, FluxUI.Space.large)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )

    }

}

@FluxPreview
@Composable
fun ArtworkPageHeader_Preview() {
    FluxThemePreview {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {

            ArtworkPageHeader(
                modifier = Modifier.fillMaxWidth(),
                fullArtwork = MediaMockups.fullMovie,
                currentMedia = MediaMockups.movie,
                showTitle = true,
                allowBlurredHeader = false,
                onImageClick = {},
                buttons = { modifier -> ArtworkButtons(modifier = modifier, media = MediaMockups.movie, sendIntent = {}) }
            )

        }
    }
}
