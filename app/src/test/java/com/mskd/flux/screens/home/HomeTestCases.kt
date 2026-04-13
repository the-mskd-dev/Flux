package com.mskd.flux.screens.home

object HomeTestCases {

    data class InitialState(
        val description: String,
        val tokenValue: String,
        val expectedSnackbarState: HomeUiState.SnackbarState
    )

}