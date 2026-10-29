package com.mskd.flux.system

import com.mskd.flux.core.model.files.UserFile

interface FilesLauncher {
    fun open(file: UserFile)

    /** Opens the file explorer on the folder where the Gallery Image Downloads are saved. */
    fun openDownloadedImages()
}