package com.mskd.flux.features.people.ui.components.people

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.people.ui.components.person.PersonPicture
import com.mskd.flux.ui.image.rememberIconPlaceholderPainter
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_person

@Composable
fun PeopleItem(
    modifier: Modifier = Modifier,
    person: Person,
    placeholder: Painter = rememberIconPlaceholderPainter(icon = Res.drawable.ic_person),
    onClick: () -> Unit = {}
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        PersonPicture(
            modifier = Modifier.fillMaxWidth(),
            name = person.name,
            profilePath = person.profilePath,
            placeholder = placeholder,
            onClick = onClick
        )

        Column(
            modifier = Modifier.padding(horizontal = FluxUI.Space.small, vertical = FluxUI.Space.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text.Adaptive(
                modifier = Modifier.fillMaxWidth(),
                text = person.role,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
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
                text = person.name,
                color = MaterialTheme.colorScheme.secondary,
                minLines = 2,
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