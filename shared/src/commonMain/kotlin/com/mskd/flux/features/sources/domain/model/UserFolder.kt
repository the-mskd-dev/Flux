package com.mskd.flux.features.sources.domain.model

import com.mskd.flux.core.model.files.FileSource

data class UserFolder(
    val path: String,
    val source: FileSource = FileSource.LOCAL,
    val isAvailable: Boolean = true
)