package com.mskd.flux.core.domain.model.catalog

import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.core.domain.model.files.UserFile

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