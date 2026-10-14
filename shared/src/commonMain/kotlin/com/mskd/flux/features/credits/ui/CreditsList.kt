package com.mskd.flux.features.credits.ui

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
import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.ui.bleedHorizontal
import com.mskd.flux.ui.dimensions.itemWidthFor
import com.mskd.flux.ui.image.rememberIconPlaceholderPainter
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_people
import flux.shared.generated.resources.seasons
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreditsList(
    credits: List<People>,
    onClick: (People) -> Unit = {}
) {

    val placeholder = rememberIconPlaceholderPainter(icon = Res.drawable.ic_people)
    var itemWidth by remember { mutableStateOf(FluxUI.Dimension.itemWidth) }
    val density = LocalDensity.current

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

            items(items = credits, key = { it.creditId }) { people ->

                PeopleItem(
                    modifier = Modifier
                        .width(itemWidth)
                        .widthIn(max = FluxUI.Dimension.itemWidth),
                    people = people,
                    placeholder = placeholder,
                    onClick = { onClick(people) }
                )

            }

        }

    }

}