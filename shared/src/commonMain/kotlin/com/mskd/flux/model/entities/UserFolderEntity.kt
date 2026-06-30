package com.mskd.flux.model.entities

import com.mskd.flux.model.FileSource

data class UserFolderEntity(
    val path: String,
    val source: FileSource
)
