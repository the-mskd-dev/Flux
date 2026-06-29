package com.mskd.flux.utils.extensions

import com.mskd.flux.model.UserFile
import com.mskd.flux.model.CatalogFolder

fun List<UserFile>.groupInFolders() : List<CatalogFolder> {
    return this
        .groupBy { file ->
            file.nameProperties.title to file.nameProperties.year
        }
        .map { (key, files) ->
            val (title, year) = key
            CatalogFolder(
                title = title,
                year = year,
                files = files
            )
        }
}