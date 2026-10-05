package com.mskd.flux.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

@Stable
interface PermissionController {
    val isGranted: Boolean
    fun request()
}

@Composable
expect fun rememberStoragePermission(onResult: (Boolean) -> Unit = {}): PermissionController

@Composable
expect fun rememberNotificationsPermission(onResult: (Boolean) -> Unit = {}): PermissionController?