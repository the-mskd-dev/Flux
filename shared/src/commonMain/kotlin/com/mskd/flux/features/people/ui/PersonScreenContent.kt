package com.mskd.flux.features.people.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.people.domain.model.PersonDetails
import com.mskd.flux.features.people.presentation.PeopleIntent
import com.mskd.flux.features.people.ui.components.person.PersonArtworks
import com.mskd.flux.features.people.ui.components.person.PersonDetails
import com.mskd.flux.features.people.ui.components.person.PersonPicture
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.fillMaxWidthWithLimit
import com.mskd.flux.ui.media.OverviewItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.biography
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonScreenContent(
    details: PersonDetails,
    sendIntent: (PeopleIntent) -> Unit
) {

    FluxScaffold(
        title = null,
        onBackTap = { sendIntent(PeopleIntent.OnBackTap) },
    ) { innerPadding ->

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidthWithLimit()
                    .padding(horizontal = FluxUI.Space.medium),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item {
                    Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                }

                item {

                    PersonPicture(
                        modifier = Modifier.width(140.dp),
                        profilePath = details.profilePath,
                        name = details.name,
                    )

                }

                item {

                    Text.MainTitle(
                        modifier = Modifier.fillMaxWidth(),
                        text = details.name,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                }

                item {
                    PersonDetails(details = details)
                }

                if (!details.biography.isNullOrBlank()) {

                    item {
                        OverviewItem(
                            title = stringResource(Res.string.biography),
                            description = details.biography,
                            fixedSize = true
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
                        .height(innerPadding.calculateBottomPadding())
                    )
                }

            }

        }

    }

}