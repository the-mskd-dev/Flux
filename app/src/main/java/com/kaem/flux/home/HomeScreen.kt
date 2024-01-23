package com.kaem.flux.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
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

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "No content",
                color = MaterialTheme.colorScheme.primary
            )

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
                    color = MaterialTheme.colorScheme.primary
                )

            }

        }

    }

}

@Composable
fun HomePermissionButton() {



}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}