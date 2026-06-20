package com.mskd.flux.shared.model

import com.mskd.flux.shared.model.artwork.ContentType

data class UserFolder(
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