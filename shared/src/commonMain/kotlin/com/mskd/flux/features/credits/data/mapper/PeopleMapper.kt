package com.mskd.flux.features.credits.data.mapper

import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.features.credits.data.model.PeopleEntity

fun PeopleEntity.toDomain(): People {
    return People(
        id = this.id,
        creditId = this.creditId,
        artworkId = this.artworkId,
        name = this.name,
        profilePath = this.profilePath,
        role = this.role,
        type = this.type,
        personOrder = this.personOrder
    )
}

fun People.toEntity(): PeopleEntity {
    return PeopleEntity(
        artworkId = this.artworkId,
        id = this.id,
        creditId = this.creditId,
        name = this.name,
        profilePath = this.profilePath,
        role = this.role,
        type = this.type,
        personOrder = this.personOrder
    )
}