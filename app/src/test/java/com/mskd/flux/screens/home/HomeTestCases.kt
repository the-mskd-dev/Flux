package com.mskd.flux.screens.home

import com.mskd.flux.utils.FluxSnackbar

object HomeTestCases {

    data class InitialState(
        val description: String,
        val tokenValue: String,
        val expectedSnackbarState: FluxSnackbar
    )

}