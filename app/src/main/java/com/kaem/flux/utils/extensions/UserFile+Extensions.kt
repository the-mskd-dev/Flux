package com.kaem.flux.utils.extensions

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.UserFolder

fun List<UserFile>.groupInFolders() : List<UserFolder> {
    return this
        .groupBy { it.nameProperties.title }
        .map { (title, files) ->
            UserFolder(title = title, files = files)
        }
}