package com.mskd.flux.model.domain.files

data class UserFolder(
    val path: String,
    val source: FileSource = FileSource.LOCAL,
    val status: Status
) {

    enum class Status { AVAILABLE, MISSING }

}