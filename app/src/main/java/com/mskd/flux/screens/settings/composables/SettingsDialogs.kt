package com.mskd.flux.screens.settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.mskd.flux.features.settings.domain.model.SettingsDialog
import com.mskd.flux.features.settings.presentation.PrivateFolderPinDialog
import com.mskd.flux.features.settings.presentation.SettingsIntent
import com.mskd.flux.features.settings.presentation.SettingsUiState
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.pin_change_new
import flux.shared.generated.resources.pin_change_old
import flux.shared.generated.resources.pin_change_title
import flux.shared.generated.resources.pin_create_subtitle
import flux.shared.generated.resources.pin_create_title
import flux.shared.generated.resources.pin_disable_subtitle
import flux.shared.generated.resources.pin_disable_title
import flux.shared.generated.resources.pin_error
import flux.shared.generated.resources.pin_field_label
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
        else -> {}
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