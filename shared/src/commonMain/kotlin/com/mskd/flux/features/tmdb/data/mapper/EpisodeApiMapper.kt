package com.mskd.flux.features.tmdb.data.mapper

import com.mskd.flux.features.tmdb.data.dto.EpisodeDto
import com.mskd.flux.model.domain.artwork.Episode
import com.mskd.flux.model.domain.artwork.Status
import com.mskd.flux.model.domain.files.UserFile

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