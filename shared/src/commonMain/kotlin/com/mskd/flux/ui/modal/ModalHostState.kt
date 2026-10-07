package com.mskd.flux.ui.modal

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.mskd.flux.ui.components.FluxDropDownMenuItem

@Stable
class ModalHostState {
    var visible by mutableStateOf(false)
        internal set
    internal var entry by mutableStateOf<ModalEntry?>(null)
    internal var owner: Any? = null
}

internal sealed class ModalEntry(
    val title: String?,
    val onDismiss: () -> Unit,
    val content: @Composable ColumnScope.() -> Unit,
) {

    class BottomSheet(title: String?, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) : ModalEntry(title, onDismiss, content)
    class Dialog(
        title: String?,
        onDismiss: () -> Unit,
        content: @Composable ColumnScope.() -> Unit,
        val onDismissLabel: String,
        val onValidate: (() -> Unit)? = null,
        val onValidateLabel: String,
    ) : ModalEntry(title, onDismiss, content)

    class Scrim(onDismiss: () -> Unit) : ModalEntry(null, onDismiss, {})

}

val LocalModalHost = staticCompositionLocalOf<ModalHostState> {
    error("ModalHostState not provided")
}