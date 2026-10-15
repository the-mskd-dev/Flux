package com.mskd.flux.features.people.ui.components.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
import flux.shared.generated.resources.original_name
import flux.shared.generated.resources.place_of_birth
import org.jetbrains.compose.resources.stringResource

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
        ) {

            if (details.originalName != null && details.originalName != details.name) {
                PersonDetailsItem(
                    text = stringResource(Res.string.original_name, details.originalName),
                )
            }

            details.birthday?.parseTMDBDate()?.formattedText?.let { birthday ->
                PersonDetailsItem(
                    text = stringResource(Res.string.born, birthday),
                )
            }

            details.deathday?.parseTMDBDate()?.formattedText?.let { deathday ->
                PersonDetailsItem(
                    text = stringResource(Res.string.died, deathday),
                )
            }

            details.placeOfBirth?.let { placeOfBirth ->
                PersonDetailsItem(
                    text = stringResource(Res.string.place_of_birth, placeOfBirth),
                )
            }

            details.knownForDepartment?.let { department ->
                PersonDetailsItem(
                    text = stringResource(Res.string.known_for_department, department),
                )
            }

        }

    }

}

@Composable
fun PersonDetailsItem(
    text: String,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
        verticalAlignment = Alignment.Top
    ) {

        Text.Content.Body(
            text = "•",
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Text.Content.Body(
            modifier = Modifier.weight(1f),
            text = text,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

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