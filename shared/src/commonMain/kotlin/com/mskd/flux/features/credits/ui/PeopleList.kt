package com.mskd.flux.features.credits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.ui.bleedHorizontal
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.seasons
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeopleList(
    peoples: List<People>
) {

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text.Content.Title(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.seasons),
            color = MaterialTheme.colorScheme.onBackground
        )


        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .bleedHorizontal(),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
            verticalAlignment = Alignment.Top
        ) {

            items(items = peoples, key = { it.id }) { people ->

                PeopleItem(people = people)

            }

        }

    }

}