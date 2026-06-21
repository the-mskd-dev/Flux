package com.mskd.flux.utils

import flux.shared.generated.resources.Res
import flux.shared.generated.resources.dark
import flux.shared.generated.resources.light
import flux.shared.generated.resources.system

object UiCommon {

    enum class THEME {
        LIGHT, DARK, SYSTEM;
        
        val stringResource get() = when(this) {
            LIGHT -> Res.string.light
            DARK -> Res.string.dark
            SYSTEM -> Res.string.system
        }
    }

}