package com.mskd.flux.features.people.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.people.domain.model.PersonDetails
import com.mskd.flux.features.people.presentation.PeopleIntent
import com.mskd.flux.features.people.ui.components.person.PersonArtworks
import com.mskd.flux.features.people.ui.components.person.PersonHeader
import com.mskd.flux.features.people.ui.components.person.PersonPicture
import com.mskd.flux.ui.media.OverviewItem
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.biography
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonContentRegular(
    details: PersonDetails,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (PeopleIntent) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FluxUI.Space.medium),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {

            Row(horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {

                PersonPicture(
                    modifier = Modifier.width(140.dp),
                    profilePath = details.profilePath,
                    name = details.name,
                )

                PersonHeader(
                    modifier = Modifier.weight(1f),
                    details = details
                )

            }

        }

        if (!details.biography.isNullOrBlank()) {

            item {
                OverviewItem(
                    title = stringResource(Res.string.biography),
                    description = details.biography
                )
            }

        }

        if (details.artworks.isNotEmpty()) {

            item {
                PersonArtworks(artworks = details.artworks, sendIntent = sendIntent)
            }

        }

        item {
            Spacer(modifier = Modifier
                .padding(bottom = FluxUI.Space.bottomScreen)
                .height(scaffoldInnerPadding.calculateBottomPadding())
            )
        }

    }

}