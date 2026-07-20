package com.mskd.flux.features.catalog.fake

import com.mskd.flux.utils.FluxSnackbar

object CatalogTestCases {

    data class InitialState(
        val description: String,
        val tokenValue: String,
        val expectedSnackbarState: FluxSnackbar
    )

}