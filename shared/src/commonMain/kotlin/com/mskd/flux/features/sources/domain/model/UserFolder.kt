package com.mskd.flux.features.sources.domain.model

import com.mskd.flux.core.model.files.FileSource

data class UserFolder(
    val path: String,
    val source: FileSource = FileSource.LOCAL,
    val status: Status = Status.AVAILABLE
) {

    enum class Status { AVAILABLE, MISSING }

}