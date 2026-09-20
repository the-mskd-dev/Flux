package com.mskd.flux.screens.privateFolder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderIntent
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderUiState
import com.mskd.flux.screens.privateFolder.composables.PinTextField
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
    isError: Boolean,
    sendIntent: (PrivateFolderIntent) -> Unit
) {

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

        PinTextField(
            isError = isError,
            onValueChange = { sendIntent(PrivateFolderIntent.ClearPinError) },
            onDone = { sendIntent(PrivateFolderIntent.SubmitPin(pin = it)) }
        )

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