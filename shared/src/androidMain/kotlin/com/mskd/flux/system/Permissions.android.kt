package com.mskd.flux.system

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberStoragePermission(onResult: (Boolean) -> Unit): PermissionController {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_VIDEO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    val state = rememberPermissionState(permission, onResult)

    return remember(state) {
        object : PermissionController {
            override val isGranted get() = state.status.isGranted
            override fun request() = state.launchPermissionRequest()
        }
    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberNotificationsPermission(onResult: (Boolean) -> Unit): PermissionController? {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null

    val state = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS, onResult)

    return remember(state) {
        object : PermissionController {
            override val isGranted get() = state.status.isGranted
            override fun request() = state.launchPermissionRequest()
        }
    }

}