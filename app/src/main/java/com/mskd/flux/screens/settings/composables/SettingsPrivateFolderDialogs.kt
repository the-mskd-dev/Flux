package com.mskd.flux.screens.settings.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
                isError = state.privateFolderPinError,
                errorMessage = stringResource(Res.string.pin_error),
                onValidate = { pin -> sendIntent(SettingsIntent.SubmitPrivateFolderPin(pin = pin)) },
                onInputStarted = { sendIntent(SettingsIntent.ClearPrivateFolderPinError) },
                onDismiss = { sendIntent(SettingsIntent.HidePrivateFolderPinDialog) }
            )
        }

        PrivateFolderPinDialog.VERIFY_TO_DISABLE -> {
            SettingsPinDialog(
                title = stringResource(Res.string.pin_disable_title),
                subtitle = stringResource(Res.string.pin_disable_subtitle),
                isError = state.privateFolderPinError,
                errorMessage = stringResource(Res.string.pin_error),
                onValidate = { pin -> sendIntent(SettingsIntent.SubmitPrivateFolderPin(pin = pin)) },
                onInputStarted = { sendIntent(SettingsIntent.ClearPrivateFolderPinError) },
                onDismiss = { sendIntent(SettingsIntent.HidePrivateFolderPinDialog) }
            )
        }

        PrivateFolderPinDialog.CHANGE_PIN -> {
            SettingsChangePinDialog(
                isError = state.privateFolderPinError,
                errorMessage = stringResource(Res.string.pin_error),
                onValidate = { oldPin, newPin ->
                    sendIntent(SettingsIntent.SubmitPrivateFolderPin(pin = oldPin, newPin = newPin))
                },
                onInputStarted = { sendIntent(SettingsIntent.ClearPrivateFolderPinError) },
                onDismiss = { sendIntent(SettingsIntent.HidePrivateFolderPinDialog) }
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
    onValidate: (String) -> Unit,
    onInputStarted: () -> Unit,
    onDismiss: () -> Unit
) {

    var pin by remember { mutableStateOf("") }

    fun submit() {
        if (pin.length != PrivateFolderPinDialog.PIN_LENGTH) return
        onValidate(pin)
    }

    // Wrong pin: the input is cleared so the user can start over
    LaunchedEffect(isError) {
        if (isError) pin = ""
    }

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { submit() },
        title = title,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {
                Text.Content.Body(text = subtitle)
                SettingsPinField(
                    label = stringResource(Res.string.pin_field_label),
                    input = pin,
                    isError = isError,
                    onValueChanged = { value ->
                        pin = value.toPinInput()
                        if (isError) onInputStarted()
                    },
                    onImeAction = { submit() }
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
fun SettingsChangePinDialog(
    isError: Boolean,
    errorMessage: String,
    onValidate: (String, String) -> Unit,
    onInputStarted: () -> Unit,
    onDismiss: () -> Unit
) {

    val focusManager = LocalFocusManager.current

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    fun submit() {
        if (oldPin.length != PrivateFolderPinDialog.PIN_LENGTH) return
        if (newPin.length != PrivateFolderPinDialog.PIN_LENGTH) return
        onValidate(oldPin, newPin)
    }

    // Wrong pin: the inputs are cleared so the user can start over
    LaunchedEffect(isError) {
        if (isError) {
            oldPin = ""
            newPin = ""
        }
    }

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { submit() },
        title = stringResource(Res.string.pin_change_title),
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {
                SettingsPinField(
                    label = stringResource(Res.string.pin_change_old),
                    input = oldPin,
                    isError = isError,
                    onValueChanged = { value ->
                        oldPin = value.toPinInput()
                        if (isError) onInputStarted()
                    },
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )
                SettingsPinField(
                    label = stringResource(Res.string.pin_change_new),
                    input = newPin,
                    isError = isError,
                    onValueChanged = { value ->
                        newPin = value.toPinInput()
                        if (isError) onInputStarted()
                    },
                    onImeAction = { submit() }
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
    onValueChanged: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit
) {

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = input,
        onValueChange = onValueChanged,
        label = { Text.List.Body(text = label) },
        isError = isError,
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        )
    )

}

private fun String.toPinInput(): String {
    return filter { it.isDigit() }.take(PrivateFolderPinDialog.PIN_LENGTH)
}
