package com.mskd.flux.utils.extensions

import com.mskd.flux.R
import com.mskd.flux.utils.UiCommon
import com.mskd.flux.utils.UiCommon.THEME.DARK
import com.mskd.flux.utils.UiCommon.THEME.LIGHT
import com.mskd.flux.utils.UiCommon.THEME.SYSTEM

val UiCommon.THEME.stringResourceId: Int get() = when(this) {
    LIGHT -> R.string.light
    DARK -> R.string.dark
    SYSTEM -> R.string.system
}