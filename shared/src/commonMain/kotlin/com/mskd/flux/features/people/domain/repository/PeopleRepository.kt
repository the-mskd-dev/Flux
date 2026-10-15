package com.mskd.flux.features.people.domain.repository

import com.mskd.flux.core.model.artwork.Person
import kotlinx.coroutines.flow.Flow

interface PeopleRepository {

    fun flowPeople(artworkId: Long): Flow<List<Person>>

    suspend fun savePeople(people: List<Person>)

    suspend fun getArtworkIdsForPerson(personId: Int): List<Long>

}