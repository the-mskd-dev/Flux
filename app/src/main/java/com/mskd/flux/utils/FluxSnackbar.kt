package com.mskd.flux.utils

import flux.shared.generated.resources.Res
import flux.shared.generated.resources.add
import flux.shared.generated.resources.see
import flux.shared.generated.resources.snackbar_add_api_key
import flux.shared.generated.resources.snackbar_see_tuto
import org.jetbrains.compose.resources.StringResource

sealed class FluxSnackbar(val id: String, val message: StringResource, val action: StringResource) {
    data object Token: FluxSnackbar(id = "token", message = Res.string.snackbar_add_api_key, action = Res.string.add)
    data object Tutorial: FluxSnackbar(id = "tutorial", message = Res.string.snackbar_see_tuto, action = Res.string.see)
}