package com.mskd.flux.features.people.data.repository

import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.people.data.dao.PersonDao
import com.mskd.flux.features.people.data.mapper.toDomain
import com.mskd.flux.features.people.data.mapper.toEntity
import com.mskd.flux.features.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PeopleRepositoryImpl(private val dao: PersonDao) : PeopleRepository {

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