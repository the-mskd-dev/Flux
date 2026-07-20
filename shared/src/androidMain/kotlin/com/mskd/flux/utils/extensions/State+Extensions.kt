package com.mskd.flux.utils.extensions

import androidx.compose.runtime.Composable
import com.mskd.flux.core.model.core.State

@Composable
fun State.Error.description(): String? {
    val message = this.message?.resolve()

    return when {
        code != null && message != null -> "CODE $code ($message)"
        code != null -> "CODE $code"
        else -> message
    }

}