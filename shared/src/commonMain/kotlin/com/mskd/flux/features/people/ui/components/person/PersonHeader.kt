package com.mskd.flux.features.people.ui.components.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.features.people.domain.model.PersonDetails
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.parseTMDBDate
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.born
import flux.shared.generated.resources.died
import flux.shared.generated.resources.known_for_department
import flux.shared.generated.resources.place_of_birth
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonHeader(
    modifier: Modifier = Modifier,
    details: PersonDetails
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {

        Text.Adaptive(
            modifier = Modifier.fillMaxWidth(),
            text = details.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = Text.Style.mainTitle(),
            overflow = TextOverflow.Ellipsis
        )

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