package com.mskd.flux.features.people.ui.components.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.people.domain.model.PersonDetails
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.ui.theme.LocalUiShapes
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.parseTMDBDate
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.born
import flux.shared.generated.resources.died
import flux.shared.generated.resources.known_for_department
import flux.shared.generated.resources.place_of_birth
import org.jetbrains.compose.resources.stringResource
import kotlin.String

@Composable
fun PersonDetails(
    details: PersonDetails
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = LocalUiShapes.current.corners,
        shadowElevation = FluxUI.Elevation.itemShadow,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = FluxUI.Space.medium),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
        ) {

            details.birthday?.parseTMDBDate()?.formattedText?.let { birthday ->
                Text.Content.Body(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.born, birthday),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            details.deathday?.parseTMDBDate()?.formattedText?.let { deathday ->
                Text.Content.Body(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.died, deathday),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            details.placeOfBirth?.let { placeOfBirth ->
                Text.Content.Body(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.place_of_birth, placeOfBirth),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            details.knownForDepartment?.let { department ->
                Text.Content.Body(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.known_for_department, department),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

        }

    }

}

@Preview
@Composable
fun PersonDetails_Preview() {
    FluxTheme {
        PersonDetails(
            details = PeopleMockup.toPersonDetails()
        )
    }
}