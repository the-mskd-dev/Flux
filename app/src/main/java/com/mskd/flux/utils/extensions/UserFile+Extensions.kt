package com.mskd.flux.utils.extensions

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.UserFolder

fun List<UserFile>.groupInFolders() : List<UserFolder> {
    return this
        .groupBy { it.nameProperties.title }
        .map { (title, files) ->
            UserFolder(title = title, files = files)
        }
}