package com.kaem.flux.screens.player

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaem.flux.screens.details.ArtworkViewModel

@Composable
fun PlayerScreen(
    onBackButtonTap: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {

    val filePath = viewModel.filePath

}