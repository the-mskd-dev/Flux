package com.kaem.flux.home

import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.ui.component.FluxButton

@Composable
fun HomeScreen() {

    val viewModel = viewModel<HomeViewModel>()
    val state by viewModel.uiState.collectAsState()

    Crossfade(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        targetState = state.isLoading,
        label = "HomeScreenAnimation"
    ) {

        when (it) {

            true -> HomeLoading()
            false -> HomeContent(shows = state.shows)


        }
        
    }


}

@Composable
fun HomeLoading() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )

    }

}

@Composable
fun HomeContent(
    shows: List<String>
) {

    if (shows.isEmpty()) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "No content",
                color = MaterialTheme.colorScheme.primary
            )

            HomePermissionButton()

        }

    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {


            shows.forEach { show ->

                Text(
                    text = show,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }

        }

    }

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomePermissionButton(viewModel: HomeViewModel = viewModel()) {

    val permissions = arrayListOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        permissions.add(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        permissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)

    val permissionsState = rememberMultiplePermissionsState(
        permissions = permissions,
        onPermissionsResult = { result ->

            Log.d("TEST", "$result")
            if (result.all { it.value }) {
                viewModel.refreshFiles()
            }

        }
    )

    if (permissionsState.allPermissionsGranted) {

        Text(
            text = "Toutes les permissions sont données",
            color = MaterialTheme.colorScheme.primary
        )

    } else {

        FluxButton(
            text = "Request permission",
            onClick = { permissionsState.launchMultiplePermissionRequest() }
        )

    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}