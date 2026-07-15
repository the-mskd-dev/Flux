package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.dto.EpisodeDto

fun EpisodeDto.toDomain(
    artworkId: Long,
    file: UserFile,
    duration: Int
) : Episode {
    return Episode(
        id = this.id,
        artworkId = artworkId,
        title = this.title,
        number = this.number,
        season = this.season,
        imagePath = this.imagePath ?: "",
        releaseDateString = this.releaseDateString,
        description = this.description,
        duration = duration,
        currentTime = 0L,
        voteAverage = this.voteAverage,
        voteCount = this.voteCount,
        status = Status.TO_WATCH,
        file = file,
        isAvailable = true
    )
}