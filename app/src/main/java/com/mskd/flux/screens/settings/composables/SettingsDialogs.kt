package com.mskd.flux.screens.settings.composables

import androidx.compose.runtime.Composable
import com.mskd.flux.features.settings.domain.model.SettingsDialog
import com.mskd.flux.features.settings.presentation.SettingsIntent
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.sync_library
import flux.shared.generated.resources.sync_library_dialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsDialogs(
    dialog: SettingsDialog?,
    sendIntent: (SettingsIntent) -> Unit
) {

    when (dialog) {
        SettingsDialog.SYNC_CATALOG -> {
            SettingsFullSyncDialog(
                sendIntent = sendIntent,
                onDismiss = { sendIntent(SettingsIntent.ShowSettingsDialog(dialog = null)) }
            )
        }
        SettingsDialog.CLEAR_HISTORY -> {
            SettingsClearHistoryDialog(
                sendIntent = sendIntent,
                onDismiss = { sendIntent(SettingsIntent.ShowSettingsDialog(dialog = null)) }
            )
        }
        null -> {}
    }

}
@Composable
fun SettingsFullSyncDialog(
    sendIntent: (SettingsIntent) -> Unit,
    onDismiss: () -> Unit
) {

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { sendIntent(SettingsIntent.ProceedFullSync) },
        title = stringResource(Res.string.sync_library),
        content = {
            Text.Content.Body(text = stringResource(Res.string.sync_library_dialog))
        }
    )

}

@Composable
fun SettingsClearHistoryDialog(
    sendIntent: (SettingsIntent) -> Unit,
    onDismiss: () -> Unit
) {

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { sendIntent(SettingsIntent.ClearHistory) },
        title = "Clear history?",
        content = {
            Text.Content.Body(text = "Voulez vous supprimer l'historique ? Cela n'affecte pas la progression de vos vidéos")
        }
    )

}