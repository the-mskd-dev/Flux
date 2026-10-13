package com.mskd.flux.features.credits.fake

import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.features.credits.domain.repository.CreditsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Behaves like the Room backed repository: [flowPeople] re-emits whenever the underlying
 * state changes, which is what happens once [savePeople] persists a new cast.
 */
class FakeCreditsRepository(
    initial: List<People> = emptyList()
) : CreditsRepository {

    private val _state = MutableStateFlow(initial)

    /** Records every call to [savePeople] so tests can assert persistence behavior. */
    val savedPeople: MutableList<List<People>> = mutableListOf()

    override fun flowPeople(artworkId: Long): Flow<List<People>> {
        return _state.map { people -> people.filter { it.artworkId == artworkId } }
    }

    override suspend fun savePeople(people: List<People>) {
        savedPeople.add(people)
        _state.value += people
    }

}