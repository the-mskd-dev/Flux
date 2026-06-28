package com.mskd.flux.utils

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.acra_dialog_comment
import flux.shared.generated.resources.acra_dialog_dismiss
import flux.shared.generated.resources.acra_dialog_message
import flux.shared.generated.resources.acra_dialog_send
import flux.shared.generated.resources.acra_dialog_title
import org.acra.dialog.CrashReportDialogHelper
import org.jetbrains.compose.resources.stringResource

class CrashDialogActivity : FragmentActivity() {

    private lateinit var helper: CrashReportDialogHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            helper = CrashReportDialogHelper(this, intent)
        } catch (_: IllegalArgumentException) {
            finish()
            return
        }

        setContent {
            FluxTheme {
                CrashDialogContent(
                    onSend = { comment ->
                        helper.sendCrash(comment, null)
                        restartApp()
                    },
                    onDismiss = {
                        helper.cancelReports()
                        restartApp()
                    }
                )
            }
        }
    }

    private fun restartApp() {
        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        finish()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashDialogContent(
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var comment by remember { mutableStateOf("") }

    Surface (
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {

        FluxDialog(
            onDismiss = onDismiss,
            onValidate = { onSend(comment) },
            title = stringResource(Res.string.acra_dialog_title),
            onDismissLabel = stringResource(Res.string.acra_dialog_dismiss),
            onValidateLabel = stringResource(Res.string.acra_dialog_send)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
            ) {

                Text.Body.Large(stringResource(Res.string.acra_dialog_message))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = {
                        Text.Body.Large(stringResource(Res.string.acra_dialog_comment))
                    },
                    textStyle = MaterialTheme.typography.bodyLarge
                )

            }

        }

    }

}

@FluxPreview
@Composable
fun CrashDialogContent_Preview() {
    FluxTheme {
        CrashDialogContent(
            onSend = {},
            onDismiss = {}
        )
    }
}