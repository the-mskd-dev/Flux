package com.mskd.flux.features.people.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.ui.bleedHorizontal
import com.mskd.flux.ui.dimensions.itemWidthFor
import com.mskd.flux.ui.image.rememberIconPlaceholderPainter
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.cast
import flux.shared.generated.resources.ic_person
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeopleList(
    people: List<Person>,
    onClick: (Person) -> Unit = {}
) {

    val placeholder = rememberIconPlaceholderPainter(icon = Res.drawable.ic_person)
    var itemWidth by remember { mutableStateOf(FluxUI.Dimension.itemWidth) }
    val density = LocalDensity.current

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text.Content.Title(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.cast),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    with(density) {
                        itemWidth = itemWidthFor(
                            screenWidthDp = size.width.toDp(),
                            columns = 3
                        )
                    }
                }
                .bleedHorizontal(),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            items(items = people, key = { it.creditId }) { person ->

                PersonItem(
                    modifier = Modifier
                        .width(itemWidth)
                        .widthIn(max = FluxUI.Dimension.itemWidth),
                    person = person,
                    placeholder = placeholder,
                    onClick = { onClick(person) }
                )

            }

        }

    }

}