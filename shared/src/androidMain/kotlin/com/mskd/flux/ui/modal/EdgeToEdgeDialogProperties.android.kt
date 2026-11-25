package com.mskd.flux.ui.modal

import androidx.compose.ui.window.DialogProperties

actual fun edgeToEdgeDialogProperties(): DialogProperties =
    DialogProperties(decorFitsSystemWindows = false)
