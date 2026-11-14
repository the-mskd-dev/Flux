package com.mskd.flux.features.gallery.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.presentation.GalleryIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.modal.bottomSheet.FluxBottomSheet
import com.mskd.flux.ui.modal.bottomSheet.FluxBottomSheetItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.clear
import flux.shared.generated.resources.gallery_filter_type
import flux.shared.generated.resources.gallery_type_backdrops
import flux.shared.generated.resources.gallery_type_logos
import flux.shared.generated.resources.gallery_type_posters
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun GalleryFilterSheet(
    isVisible: Boolean,
    selectedTypes: Set<GalleryImageType>,
    sendIntent: (GalleryIntent) -> Unit,
) {

    val clearButtonAlpha by animateFloatAsState(if (selectedTypes.isEmpty()) 0f else 1f)

    FluxBottomSheet(
        isVisible = isVisible,
        onDismiss = { sendIntent(GalleryIntent.OnFilterDismiss) },
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FluxUI.Space.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text.List.Title(text = stringResource(Res.string.gallery_filter_type))

            TextButton(
                modifier = Modifier.alpha(clearButtonAlpha),
                onClick = { if (selectedTypes.isNotEmpty()) sendIntent(GalleryIntent.OnFilterClearTap) }
            ) {
                Text.Button.Default(text = stringResource(Res.string.clear))
            }

        }

        GalleryImageType.entries.forEach { type ->
            FluxBottomSheetItem(
                isSelected = type in selectedTypes,
                text = stringResource(type.label()),
                onClick = { sendIntent(GalleryIntent.OnFilterTypeTap(type)) }
            )
        }

    }

}

private fun GalleryImageType.label(): StringResource = when (this) {
    GalleryImageType.BACKDROP -> Res.string.gallery_type_backdrops
    GalleryImageType.POSTER -> Res.string.gallery_type_posters
    GalleryImageType.LOGO -> Res.string.gallery_type_logos
}

@FluxPreview
@Composable
fun GalleryFilterSheet_Preview() {
    FluxThemePreview {
        GalleryFilterSheet(
            isVisible = true,
            selectedTypes = setOf(GalleryImageType.POSTER),
            sendIntent = {}
        )
    }
}
