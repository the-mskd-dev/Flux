package com.mskd.flux.features.people.fake

import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Behaves like the Room backed repository: [flowPeople] re-emits whenever the underlying
 * state changes, which is what happens once [savePeople] persists a new cast.
 */
class FakePeopleRepository(
    initial: List<Person> = emptyList()
) : PeopleRepository {

    private val _state = MutableStateFlow(initial)

    /** Records every call to [savePeople] so tests can assert persistence behavior. */
    val savedPeople: MutableList<List<Person>> = mutableListOf()

    override fun flowPeople(artworkId: Long): Flow<List<Person>> {
        return _state.map { people -> people.filter { it.artworkId == artworkId } }
    }

    override suspend fun savePeople(people: List<Person>) {
        savedPeople.add(people)
        _state.value += people
    }

    override suspend fun getArtworkIdsForPerson(personId: Int): List<Long> {
        return _state.value
            .filter { it.id == personId }
            .map { it.artworkId }
            .distinct()
    }

}