package com.mskd.flux.features.people.data.repository

import app.cash.turbine.test
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.features.people.data.dao.PersonDao
import com.mskd.flux.features.people.data.mapper.toEntity
import com.mskd.flux.features.people.data.model.PersonEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class PeopleRepositoryImplTest : FunSpec({

    lateinit var dao: PersonDao
    lateinit var repository: PeopleRepositoryImpl

    val artworkId = 550L

    beforeTest {
        dao = mockk(relaxed = true)
        repository = PeopleRepositoryImpl(dao = dao)
    }

    test("flowPerson should map entities to domain and sort cast before crew by order") {
        // Given
        val crew = PersonEntity(artworkId, 2, "c2", "Arnon Milchan", null, "Executive Producer", PersonType.CREW, personOrder = 0)
        val cast1 = PersonEntity(artworkId, 1, "c1", "Edward Norton", "/p.jpg", "The Narrator", PersonType.CAST, personOrder = 1)
        val cast0 = PersonEntity(artworkId, 3, "c3", "Meat Loaf", "/p2.jpg", "Robert Paulson", PersonType.CAST, personOrder = 0)
        every { dao.flowPerson(artworkId) } returns MutableStateFlow(listOf(crew, cast1, cast0))

        // When / Then
        repository.flowPeople(artworkId).test {
            val result = awaitItem()
            // CAST (ordinal 0) first, ordered by personOrder, then CREW (ordinal 1)
            result.map { it.creditId } shouldBe listOf("c3", "c1", "c2")
            result[0].type shouldBe PersonType.CAST
            result[2].type shouldBe PersonType.CREW
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("flowPerson should only return person of the requested artwork") {
        // Given : the DAO applies the WHERE artworkId = :artworkId clause
        val other = PersonEntity(artworkId = 999L, id = 5, creditId = "x1", name = "Other", profilePath = null, role = "Role", type = PersonType.CAST, personOrder = 0)
        val mine = PersonEntity(artworkId, id = 6, creditId = "y1", name = "Mine", profilePath = null, role = "Role", type = PersonType.CAST, personOrder = 0)
        val all = listOf(other, mine)
        every { dao.flowPerson(any()) } answers {
            val queriedId = firstArg<Long>()
            MutableStateFlow(all.filter { it.artworkId == queriedId })
        }

        // When / Then
        repository.flowPeople(artworkId).test {
            awaitItem() shouldBe listOf(
                Person(id = 6, creditId = "y1", artworkId = artworkId, name = "Mine", profilePath = null, role = "Role", type = PersonType.CAST, personOrder = 0)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("savePerson should convert to entities and call insert from the dao") {
        // Given
        val person = listOf(
            Person(id = 1, creditId = "c1", artworkId = artworkId, name = "Edward Norton", profilePath = "/p.jpg", role = "The Narrator", type = PersonType.CAST, personOrder = 0)
        )

        // When
        repository.savePeople(people = person)

        // Then
        coVerify { dao.insert(person.map { it.toEntity() }) }
    }

})