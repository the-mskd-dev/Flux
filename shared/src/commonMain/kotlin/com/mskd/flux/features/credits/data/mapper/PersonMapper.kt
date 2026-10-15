package com.mskd.flux.features.credits.data.mapper

import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.credits.data.model.PersonEntity

fun PersonEntity.toDomain(): Person {
    return Person(
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

fun Person.toEntity(): PersonEntity {
    return PersonEntity(
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