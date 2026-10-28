package com.mskd.flux.ui

import android.os.Build

actual val supportsNativeBlur: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
