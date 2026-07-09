package com.mskd.flux.core.model.catalog

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.files.UserFile

data class CatalogFolder(
    val title: String,
    val year: Int? = null,
    val files: List<UserFile>
) {

    val type: ContentType?
        get() = when {
            files.all { it.isEpisode } -> ContentType.SHOW
            files.size == 1 && !files.first().isEpisode -> ContentType.MOVIE
            else -> null
        }

}