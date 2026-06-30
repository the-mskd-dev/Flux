package com.mskd.flux.model.domain

import com.mskd.flux.model.domain.artwork.ContentType

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