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
                input = state.privateFolderPinInput.primary,
                isError = state.privateFolderPinError,
                errorMessage = stringResource(Res.string.pin_error),
                onValueChanged = { sendIntent(SettingsIntent.OnPrivateFolderPinChanged(primary = it)) },
                onValidate = { sendIntent(SettingsIntent.SubmitPrivateFolderPin) },
                onDismiss = { sendIntent(SettingsIntent.HidePrivateFolderPinDialog) }
            )
        }

        PrivateFolderPinDialog.VERIFY_TO_DISABLE -> {
            SettingsPinDialog(
                title = stringResource(Res.string.pin_disable_title),
                subtitle = stringResource(Res.string.pin_disable_subtitle),
                input = state.privateFolderPinInput.primary,
                isError = state.privateFolderPinError,
                errorMessage = stringResource(Res.string.pin_error),
                onValueChanged = { sendIntent(SettingsIntent.OnPrivateFolderPinChanged(primary = it)) },
                onValidate = { sendIntent(SettingsIntent.SubmitPrivateFolderPin) },
                onDismiss = { sendIntent(SettingsIntent.HidePrivateFolderPinDialog) }
            )
        }

        PrivateFolderPinDialog.CHANGE_PIN -> {
            FluxDialog(
                onDismiss = { sendIntent(SettingsIntent.HidePrivateFolderPinDialog) },
                onValidate = { sendIntent(SettingsIntent.SubmitPrivateFolderPin) },
                title = stringResource(Res.string.pin_change_title),
                content = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                    ) {
                        SettingsPinField(
                            label = stringResource(Res.string.pin_change_old),
                            input = state.privateFolderPinInput.primary,
                            isError = state.privateFolderPinError,
                            onValueChanged = {
                                sendIntent(
                                    SettingsIntent.OnPrivateFolderPinChanged(
                                        primary = it,
                                        secondary = state.privateFolderPinInput.secondary
                                    )
                                )
                            }
                        )
                        SettingsPinField(
                            label = stringResource(Res.string.pin_change_new),
                            input = state.privateFolderPinInput.secondary,
                            isError = state.privateFolderPinError,
                            onValueChanged = {
                                sendIntent(
                                    SettingsIntent.OnPrivateFolderPinChanged(
                                        primary = state.privateFolderPinInput.primary,
                                        secondary = it
                                    )
                                )
                            }
                        )
                        if (state.privateFolderPinError) {
                            Text.Content.Body(
                                text = stringResource(Res.string.pin_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }

        null -> {}
    }

}

@Composable
fun SettingsPinDialog(
    title: String,
    subtitle: String,
    input: String,
    isError: Boolean,
    errorMessage: String,
    onValueChanged: (String) -> Unit,
    onValidate: () -> Unit,
    onDismiss: () -> Unit
) {

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { onValidate() },
        title = title,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {
                Text.Content.Body(text = subtitle)
                SettingsPinField(
                    label = stringResource(Res.string.pin_field_label),
                    input = input,
                    isError = isError,
                    onValueChanged = onValueChanged
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

@Composable
fun SettingsPinField(
    label: String,
    input: String,
    isError: Boolean,
    onValueChanged: (String) -> Unit
) {

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = input,
        onValueChange = onValueChanged,
        label = { Text.List.Body(text = label) },
        isError = isError,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
    )

}