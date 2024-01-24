package com.kaem.flux.model

import android.net.Uri

sealed class FileSource(
    val name: String,
    val addedDateString: String
) {

    class Local(name: String, addedDateString: String, val uri: Uri) : FileSource(name, addedDateString)
    class GDrive(name: String, addedDateString: String, val path: String) : FileSource(name, addedDateString)

}