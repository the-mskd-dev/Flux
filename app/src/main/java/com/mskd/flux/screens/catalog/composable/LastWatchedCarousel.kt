package com.mskd.flux.screens.catalog.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.ui.theme.FluxUI
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastWatchedCarousel(
    artworks: List<Artwork>,
    sendIntent: (CatalogIntent) -> Unit
) {

    if (artworks.isEmpty())
        return

    val ratio = 1920f/1080f


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (artworks.size == 1) {

            val overview = artworks.first()
            val path = overview.bannerPath

            MediaItem(
                modifier = Modifier
                    .widthIn(max = 350.dp)
                    .fillMaxSize(),
                path = path,
                shape = MaterialTheme.shapes.extraLarge,
                ratio = ratio,
                onClick = { rgb -> sendIntent(CatalogIntent.OnArtworkTap(artwork = overview, rgb = rgb)) },
                description = overview.title
            )

        } else {

            val carouselState = rememberCarouselState { artworks.size }
            val scope = rememberCoroutineScope()

            HorizontalCenteredHeroCarousel(
                modifier = Modifier.fillMaxWidth(),
                maxItemWidth = 350.dp,
                state = carouselState,
                contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
            ) { i ->

                val overview = artworks[i]
                val path = overview.bannerPath

                Box(modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge)) {
                    MediaItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(ratio),
                        path = path,
                        ratio = ratio,
                        shape = MaterialTheme.shapes.extraLarge,
                        onClick = { rgb ->

                            if (carouselState.currentItem != i) {
                                scope.launch { carouselState.animateScrollToItem(i) }
                            } else {
                                sendIntent(CatalogIntent.OnArtworkTap(artwork = overview, rgb = rgb))
                            }

                        },
                        description = overview.title
                    )
                }


            }

            CarouselIndicator(
                itemCount = artworks.size,
                currentPage = carouselState.currentItem
            )

        }

    }

}

@Composable
fun CarouselIndicator(
    itemCount: Int,
    currentPage: Int,
) {

    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f)

    Row(
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(itemCount) { index ->

            val color by animateColorAsState(if (currentPage == index) selectedColor else unselectedColor)

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }

}

