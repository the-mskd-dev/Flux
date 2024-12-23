package com.kaem.flux.screens.library

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxButton

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryPermissionScreen(permissionState: PermissionState) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        FluxButton(
            text = stringResource(id = R.string.give_permission),
            onClick = { permissionState.launchPermissionRequest() }
        )

    }

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun libraryPermissionState(): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        android.Manifest.permission.READ_MEDIA_VIDEO
    else
        android.Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission)

}