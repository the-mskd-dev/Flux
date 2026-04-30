package com.mskd.flux.utils

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.mskd.flux.R
import com.mskd.flux.ui.component.FluxDialog
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import org.acra.dialog.CrashReportDialogHelper

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
            AppTheme {
                CrashDialogContent(
                    onSend = { comment ->
                        helper.sendCrash(comment, null)
                        finish()
                    },
                    onDismiss = {
                        helper.cancelReports()
                        finish()
                    }
                )
            }
        }
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
            title = stringResource(R.string.acra_dialog_title),
            onDismissLabel = stringResource(R.string.acra_dialog_dismiss),
            onValidateLabel = stringResource(R.string.acra_dialog_send)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                Text.Body.Large(stringResource(R.string.acra_dialog_message))

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = {
                        Text.Body.Large(stringResource(R.string.acra_dialog_comment))
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
    AppTheme {
        CrashDialogContent(
            onSend = {},
            onDismiss = {}
        )
    }
}