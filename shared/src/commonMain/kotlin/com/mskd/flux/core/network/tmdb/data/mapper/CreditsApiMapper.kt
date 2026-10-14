package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.core.model.artwork.PeopleType
import com.mskd.flux.core.network.tmdb.data.dto.credits.CreditsDto

/**
 * Maps a TMDB credits response to the unified [People] model.
 *
 * Cast members keep their billing [CastMemberDto.order] while crew members use their
 * index, so a single table/object can hold both cast and crew with a stable ordering.
 */
fun CreditsDto.toDomain(artworkId: Long): List<People> {

    val castPeople = this.cast.map { member ->
        People(
            id = member.id,
            creditId = member.creditId,
            artworkId = artworkId,
            name = member.name,
            profilePath = member.profilePath,
            role = member.character,
            type = PeopleType.CAST,
            personOrder = member.order
        )
    }

    val crewPeople = this.crew.mapIndexed { index, member ->
        People(
            id = member.id,
            creditId = member.creditId,
            artworkId = artworkId,
            name = member.name,
            profilePath = member.profilePath,
            role = member.job,
            type = PeopleType.CREW,
            personOrder = index
        )
    }

    return castPeople + crewPeople
}