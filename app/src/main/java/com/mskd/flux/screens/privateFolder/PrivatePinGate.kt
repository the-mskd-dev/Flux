package com.mskd.flux.screens.privateFolder

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderIntent
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderUiState
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_lock
import flux.shared.generated.resources.pin_gate_title
import flux.shared.generated.resources.private_folder
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivatePinGate(
    modifier: Modifier = Modifier,
    pinError: Boolean,
    sendIntent: (PrivateFolderIntent) -> Unit
) {

    val pinLength = 4

    var pinInput by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun submit(pin: String) {
        if (pin.length != pinLength) return
        sendIntent(PrivateFolderIntent.SubmitPin(pin = pin))
    }

    // The hidden field below keeps the focus forever, so the keyboard never closes
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Wrong pin: the input is reset, the error is only displayed by the fields themselves
    LaunchedEffect(pinError) {
        if (pinError) pinInput = TextFieldValue()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = FluxUI.Space.medium),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(Res.drawable.ic_lock),
            contentDescription = stringResource(Res.string.private_folder),
            tint = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(FluxUI.Space.large))

        Text.Content.Body(
            text = stringResource(Res.string.pin_gate_title),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(FluxUI.Space.medium))

        Box {

            Row(
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {

                repeat(pinLength) { index ->

                    OutlinedTextField(
                        modifier = Modifier
                            .width(40.dp)
                            .focusProperties { canFocus = false },
                        value = pinInput.text.getOrNull(index)?.toString().orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        isError = pinError,
                        singleLine = true,
                        textStyle = Text.Style.contentBody().copy(textAlign = TextAlign.Center),
                        visualTransformation = PasswordVisualTransformation()
                    )

                }

            }

            // Hidden field owning the focus: the input is captured there, the fields above only display it
            BasicTextField(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .focusRequester(focusRequester),
                value = pinInput,
                onValueChange = { value ->
                    val digits = value.text.filter { it.isDigit() }.take(pinLength)
                    val normalized = TextFieldValue(text = digits, selection = TextRange(digits.length))

                    if (digits != pinInput.text) {
                        pinInput = normalized

                        // A new input starts: the previous error is no longer relevant
                        if (pinError) sendIntent(PrivateFolderIntent.ClearPinError)

                        submit(pin = digits) // Validates automatically on the last digit
                    } else if (pinInput != normalized) {
                        pinInput = normalized // Keep the caret at the end
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { submit(pin = pinInput.text) }
                )
            )

            // Tapping the fields keeps the hidden one focused, so the keyboard stays open
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }
            )

        }

    }

}

@FluxPreview
@Composable
fun PrivateScreen_Locked_Preview() {
    FluxTheme {
        PrivateScreenContent(
            state = PrivateFolderUiState(locked = true),
            sendIntent = {}
        )
    }
}