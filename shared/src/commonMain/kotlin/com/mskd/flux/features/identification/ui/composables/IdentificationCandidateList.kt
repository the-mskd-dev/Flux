package com.mskd.flux.features.identification.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.identification.presentation.IdentificationCandidate
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.image.rememberIconPlaceholderPainter
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_images
import flux.shared.generated.resources.identification_current
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun IdentificationCandidateList(
    candidates: ImmutableList<IdentificationCandidate>,
    onSelect: (IdentificationCandidate) -> Unit,
) {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
    ) {
        items(items = candidates, key = { it.artwork.id }) { candidate ->
            CandidateItem(
                candidate = candidate,
                onClick = { onSelect(candidate) },
            )
        }
    }

}

@Composable
private fun CandidateItem(
    candidate: IdentificationCandidate,
    onClick: () -> Unit,
) {

    val placeholder = rememberIconPlaceholderPainter(icon = Res.drawable.ic_images)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !candidate.isCurrent, onClick = onClick)
            .alpha(if (candidate.isCurrent) .5f else 1f)
            .padding(FluxUI.Space.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
    ) {

        FluxImage(
            modifier = Modifier
                .width(56.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp)),
            path = candidate.artwork.imagePath,
            contentDescription = candidate.artwork.title,
            placeholder = placeholder,
            error = placeholder,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
        ) {
            Text.List.Title(text = candidate.artwork.title, maxLines = 2)
            Text.List.Body(text = candidate.year?.toString())
            if (candidate.isCurrent) {
                Text.Content.Label(
                    text = stringResource(Res.string.identification_current),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

    }

}
