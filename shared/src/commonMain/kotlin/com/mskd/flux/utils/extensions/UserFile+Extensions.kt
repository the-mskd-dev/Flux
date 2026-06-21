package com.mskd.flux.utils.extensions

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.UserFolder

fun List<UserFile>.groupInFolders() : List<UserFolder> {
    return this
        .groupBy { file ->
            file.nameProperties.title to file.nameProperties.year
        }
        .map { (key, files) ->
            val (title, year) = key
            UserFolder(
                title = title,
                year = year,
                files = files
            )
        }
}