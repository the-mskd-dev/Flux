package com.kaem.flux.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaem.flux.screens.home.LibraryViewModel

@Composable
fun DetailsScreen(
    artworkId: Int,
    viewModel: DetailsViewModel = hiltViewModel()
) {

    val artwork = viewModel.getArtworks(artworkId)

    /*Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {



    }*/

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = artwork?.title ?: "DetailsScreen",
            color = MaterialTheme.colorScheme.onBackground
        )

    }

}