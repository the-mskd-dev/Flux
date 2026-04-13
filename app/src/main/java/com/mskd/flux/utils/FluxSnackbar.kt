package com.mskd.flux.utils

import com.mskd.flux.R

sealed class FluxSnackbar(val id: String, val message: Int, val action: Int) {
    data object Token: FluxSnackbar(id = "token", message = R.string.snackbar_add_api_key, action = R.string.add)
    data object Tutorial: FluxSnackbar(id = "tutorial", message = R.string.snackbar_see_tuto, action = R.string.see)
}