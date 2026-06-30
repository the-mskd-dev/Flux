package com.mskd.flux.model.data.remote.tmdb.mappers

import com.mskd.flux.model.data.remote.tmdb.dto.EpisodeDto
import com.mskd.flux.model.domain.Status
import com.mskd.flux.model.domain.UserFile
import com.mskd.flux.model.domain.artwork.Episode

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
        file = file
    )
}