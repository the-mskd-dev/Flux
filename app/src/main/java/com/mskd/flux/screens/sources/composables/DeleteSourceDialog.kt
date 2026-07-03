package com.mskd.flux.screens.sources.composables

import androidx.compose.runtime.Composable
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.screen.sources.SourcesIntent
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.confirm_source_deletion
import flux.shared.generated.resources.delete
import flux.shared.generated.resources.warning
import org.jetbrains.compose.resources.stringResource

@Composable
fun DeleteSourceDialog(
    folder: UserFolder,
    sendIntent: (SourcesIntent) -> Unit
) {

    FluxDialog(
        title = stringResource(Res.string.warning),
        content = {
            Text.Body.Large(
                text = stringResource(Res.string.confirm_source_deletion)
            )
        },
        onDismiss = { sendIntent(SourcesIntent.CloseDeleteDialog) },
        onValidate = { sendIntent(SourcesIntent.DeleteFolder(folder = folder)) },
        onValidateLabel = stringResource(Res.string.delete),
    )


}