package com.kaem.flux.model

import android.net.Uri

sealed class FluxSource {
    class Local(val uri: Uri) : FluxSource()
    class GDrive(val path: String) : FluxSource()
}
