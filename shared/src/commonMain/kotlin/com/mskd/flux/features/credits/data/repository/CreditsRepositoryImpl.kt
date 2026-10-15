package com.mskd.flux.features.credits.data.repository

import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.credits.data.dao.PersonDao
import com.mskd.flux.features.credits.data.mapper.toDomain
import com.mskd.flux.features.credits.data.mapper.toEntity
import com.mskd.flux.features.credits.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CreditsRepositoryImpl(private val dao: PersonDao) : CreditsRepository {

    override fun flowPeople(artworkId: Long): Flow<List<Person>> {
        return dao.flowPerson(artworkId = artworkId)
            .map { entities ->
                entities
                    .map { it.toDomain() }
                    .sortedWith(compareBy({ it.type.ordinal }, { it.personOrder }))
            }
    }

    override suspend fun savePeople(people: List<Person>) {
        dao.insert(people = people.map { it.toEntity() })
    }

    override suspend fun getArtworkIdsForPerson(personId: Int): List<Long> {
        return dao.getArtworkIdsForPerson(personId = personId)
    }

}