package com.mskd.flux.model.domain.files

data class UserFolder(
    val path: String,
    val source: FileSource,
    val status: Status
) {

    enum class Status { AVAILABLE, MISSING }

}