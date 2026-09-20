package com.mskd.flux.screens.settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.mskd.flux.features.settings.presentation.PrivateFolderPinDialog
import com.mskd.flux.features.settings.presentation.SettingsIntent
import com.mskd.flux.features.settings.presentation.SettingsUiState
import com.mskd.flux.screens.privateFolder.composables.PinTextField
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.pin_create_subtitle
import flux.shared.generated.resources.pin_create_title
import flux.shared.generated.resources.pin_disable_subtitle
import flux.shared.generated.resources.pin_disable_title
import flux.shared.generated.resources.pin_error
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsPrivateFolderDialogs(
    state: SettingsUiState,
    sendIntent: (SettingsIntent) -> Unit
) {

    when (state.privateFolderPinDialog) {
        PrivateFolderPinDialog.CREATE -> {
            SettingsPinDialog(
                title = stringResource(Res.string.pin_create_title),
                subtitle = stringResource(Res.string.pin_create_subtitle),
                isError = state.privateFolderPinError,
                errorMessage = stringResource(Res.string.pin_error),
                sendIntent = sendIntent,
            )
        }

        PrivateFolderPinDialog.VERIFY_TO_DISABLE -> {
            SettingsPinDialog(
                title = stringResource(Res.string.pin_disable_title),
                subtitle = stringResource(Res.string.pin_disable_subtitle),
                isError = state.privateFolderPinError,
                errorMessage = stringResource(Res.string.pin_error),
                sendIntent = sendIntent,
            )
        }

        null -> {}
    }

}

@Composable
fun SettingsPinDialog(
    title: String,
    subtitle: String,
    isError: Boolean,
    errorMessage: String,
    sendIntent: (SettingsIntent) -> Unit,
) {

    val keyboard = LocalSoftwareKeyboardController.current
    var pin by remember { mutableStateOf("") }

    FluxDialog(
        onDismiss = { sendIntent(SettingsIntent.HidePrivateFolderPinDialog) },
        onValidate = {
            keyboard?.hide()
            sendIntent(SettingsIntent.SubmitPrivateFolderPin(pin = pin))
        },
        title = title,
        content = {

            Column(
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {

                Text.Content.Body(text = subtitle)

                PinTextField(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    isError = isError,
                    hidePin = false,
                    onValueChange = {
                        sendIntent(SettingsIntent.ClearPrivateFolderPinError)
                        pin = it
                    }
                )

                if (isError) {
                    Text.Content.Body(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )

}