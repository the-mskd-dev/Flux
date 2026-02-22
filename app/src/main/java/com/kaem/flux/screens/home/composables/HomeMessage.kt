package com.kaem.flux.screens.home.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.mockups.FirebaseMockups
import com.kaem.flux.model.remoteConfig.Message
import com.kaem.flux.screens.home.HomeIntent
import com.kaem.flux.ui.component.FluxDialog
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme


@Composable
fun HomeMessage(
    message: Message,
    sendIntent: (HomeIntent) -> Unit
) {

    AlertDialog(
        onDismissRequest = { sendIntent(HomeIntent.CloseMessage) },
        confirmButton = {
            TextButton(
                onClick = { sendIntent(HomeIntent.CloseMessage) },
                content = {
                    Text.Label.Large(text = stringResource(R.string.close))
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = { sendIntent(HomeIntent.DoNotShowMessage) },
                content = {
                    Text.Label.Large(text = stringResource(R.string.do_not_show_again))
                }
            )
        },
        title = { Text.Headline.Small(text = message.title) },
        text = { Text.Label.Medium(text = message.message) }
    )

}

@Preview
@Composable
fun HomeMessage_Preview() {
    AppTheme {
        HomeMessage(
            message = FirebaseMockups.message,
            sendIntent = {}
        )
    }
}