package com.mskd.flux.model.data.local.entities

import com.mskd.flux.model.domain.FileSource

data class UserFolderEntity(
    val path: String,
    val source: FileSource
)
