package com.mskd.flux.features.artwork.ui.composables

import androidx.compose.runtime.Composable
import com.mskd.flux.features.identification.ui.IdentificationDialog
import com.mskd.flux.ui.components.ResetProgressDialog

/**
 * Dialogs shared by the Artwork pages: the Manual identification of the Artwork, and the reset of its progression.
 */
@Composable
fun ArtworkPageDialogs(
    artworkId: Long,
    isIdentificationVisible: Boolean,
    isResetProgressVisible: Boolean,
    onDismiss: () -> Unit,
    onIdentified: (artworkId: Long) -> Unit,
    onResetProgress: () -> Unit,
) {

    if (isIdentificationVisible) {
        IdentificationDialog(
            artworkId = artworkId,
            onDismiss = onDismiss,
            onIdentified = onIdentified,
        )
    }

    if (isResetProgressVisible) {
        ResetProgressDialog(
            onValidate = onResetProgress,
            onDismiss = onDismiss
        )
    }

}
