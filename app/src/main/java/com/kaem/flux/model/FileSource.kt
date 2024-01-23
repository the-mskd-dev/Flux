package com.kaem.flux.model

import android.net.Uri

sealed class FileSource(val name: String) {

    class Local(name: String, val uri: Uri) : FileSource(name)
    class GDrive(name: String, val path: String) : FileSource(name)

}