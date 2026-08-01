package com.mskd.flux.utils

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun storagePermissionState(
    onPermissionResult: (Boolean) -> Unit = {}
): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_VIDEO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission, onPermissionResult = onPermissionResult)

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun notificationsPermissionState(onPermissionResult: (Boolean) -> Unit = {}): PermissionState? {

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS, onPermissionResult = onPermissionResult)
    else
        null

}