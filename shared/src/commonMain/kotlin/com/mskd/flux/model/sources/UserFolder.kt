package com.mskd.flux.model.sources

import com.mskd.flux.model.FileSource

data class UserFolder(
    val path: String,
    val source: FileSource
)
