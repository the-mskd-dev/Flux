package com.mskd.flux.utils

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun storagePermissionState(): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_VIDEO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission)

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun notificationsPermissionState(): PermissionState? {

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    else
        null

}