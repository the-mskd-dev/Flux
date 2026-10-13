package com.mskd.flux.features.credits.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.tmdbImage

@Composable
fun PeopleItem(people: People, ) {

    Column(
        modifier = Modifier.width(FluxUI.Dimension.itemWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(FluxUI.Dimension.itemRatio),
            shape = FluxUI.shapes.corners,
            shadowElevation = FluxUI.Elevation.itemShadow
        ) {

            FluxImage(
                modifier = Modifier.fillMaxSize(),
                path = people.profilePath?.tmdbImage.orEmpty(),
                contentDescription = people.name
            )

        }

        Column(
            modifier = Modifier.padding(horizontal = FluxUI.Space.small, vertical = FluxUI.Space.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text.Adaptive(
                modifier = Modifier.fillMaxWidth(),
                text = people.role,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = MaterialTheme.typography.titleSmall.fontSize,
                    minFontSize = MaterialTheme.typography.labelSmall.fontSize
                ),
            )

            Text.Adaptive(
                modifier = Modifier.fillMaxWidth(),
                text = people.name,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                autoSize = TextAutoSize.StepBased(
                    maxFontSize = MaterialTheme.typography.titleSmall.fontSize,
                    minFontSize = MaterialTheme.typography.labelSmall.fontSize
                ),
            )

        }

    }

}