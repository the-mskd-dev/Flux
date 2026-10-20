package com.mskd.flux.utils.extensions

import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile

fun List<UserFile>.groupInFolders() : List<CatalogFolder> {
    return this
        .groupBy { file ->
            // The TMDB ID identifies the artwork whatever the name: the title only groups files without ID
            val properties = file.nameProperties
            if (properties.tmdbId != null) Triple(properties.tmdbId, null, null)
            else Triple(null, properties.title, properties.year)
        }
        .map { (key, files) ->
            val properties = files.first().nameProperties
            CatalogFolder(
                title = properties.title,
                year = properties.year,
                files = files,
                tmdbId = key.first
            )
        }
}