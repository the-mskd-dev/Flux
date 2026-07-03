package com.mskd.flux.features.sources.domain.model

import com.mskd.flux.model.domain.files.FileSource

data class UserFolder(
    val path: String,
    val source: FileSource = FileSource.LOCAL,
    val status: Status
) {

    enum class Status { AVAILABLE, MISSING }

}