package com.mskd.flux.features.credits.data.repository

import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.features.credits.data.dao.PeopleDao
import com.mskd.flux.features.credits.data.mapper.toDomain
import com.mskd.flux.features.credits.data.mapper.toEntity
import com.mskd.flux.features.credits.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CreditsRepositoryImpl(private val dao: PeopleDao) : CreditsRepository {

    override fun flowPeople(artworkId: Long): Flow<List<People>> {
        return dao.flowPeople(artworkId = artworkId)
            .map { entities ->
                entities
                    .map { it.toDomain() }
                    .sortedWith(compareBy({ it.type.ordinal }, { it.personOrder }))
            }
    }

    override suspend fun savePeople(people: List<People>) {
        dao.insert(people = people.map { it.toEntity() })
    }

}