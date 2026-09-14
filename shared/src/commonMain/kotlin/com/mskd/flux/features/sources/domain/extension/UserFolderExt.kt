package com.mskd.flux.features.sources.domain.extension

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.sources.domain.model.UserFolder

fun List<UserFolder>.findForFile(file: UserFile) : UserFolder? {
    return this.find { file.path.startsWith(it.path) }
}

fun List<UserFolder>.isAvailableFor(file: UserFile): Boolean {
    return when (file.source) {
        FileSource.LOCAL -> true
        FileSource.SAF -> findForFile(file = file)?.isAvailable ?: false
    }
}