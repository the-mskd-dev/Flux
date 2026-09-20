package com.mskd.flux.screens.privateFolder.composables

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.settings.presentation.PrivateFolderPinDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun PinTextField(
    modifier: Modifier = Modifier,
    isError: Boolean,
    hidePin: Boolean = true,
    onInput: () -> Unit,
    onDone: (String) -> Unit
) {

    val pinLength = PrivateFolderPinDialog.PIN_LENGTH
    var pinInput by remember { mutableStateOf(TextFieldValue()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Open automatically the keyboard
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Wrong pin: the input is reset, the error is only displayed by the fields themselves
    LaunchedEffect(isError) {
        if (isError) pinInput = TextFieldValue()
    }

    Box(modifier = modifier) {

        Row(horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)) {

            repeat(pinLength) { index ->

                OutlinedTextField(
                    modifier = Modifier
                        .width(56.dp)
                        .focusProperties { canFocus = false },
                    value = pinInput.text.getOrNull(index)?.toString().orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    isError = isError,
                    singleLine = true,
                    textStyle = Text.Style.contentBody().copy(textAlign = TextAlign.Center),
                    visualTransformation = if (hidePin) PasswordVisualTransformation() else VisualTransformation.None
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
                    if (isError) onInput()

                    if (digits.length == pinLength)
                        onDone(digits) // Validates automatically on the last digit
                } else if (pinInput != normalized) {
                    pinInput = normalized // Keep the cursor at the end
                }

            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (pinInput.text.length == pinLength) onDone(pinInput.text) }
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