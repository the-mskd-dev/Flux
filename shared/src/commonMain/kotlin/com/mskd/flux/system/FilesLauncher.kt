package com.mskd.flux.system

import com.mskd.flux.core.model.files.UserFile

interface FilesLauncher {
    fun open(file: UserFile)
}