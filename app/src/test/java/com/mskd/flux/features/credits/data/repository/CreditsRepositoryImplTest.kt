package com.mskd.flux.features.credits.data.repository

import app.cash.turbine.test
import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.core.model.artwork.PeopleType
import com.mskd.flux.features.credits.data.dao.PeopleDao
import com.mskd.flux.features.credits.data.mapper.toEntity
import com.mskd.flux.features.credits.data.model.PeopleEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class CreditsRepositoryImplTest : FunSpec({

    lateinit var dao: PeopleDao
    lateinit var repository: CreditsRepositoryImpl

    val artworkId = 550L

    beforeTest {
        dao = mockk(relaxed = true)
        repository = CreditsRepositoryImpl(dao = dao)
    }

    test("flowPeople should map entities to domain and sort cast before crew by order") {
        // Given
        val crew = PeopleEntity(artworkId, 2, "c2", "Arnon Milchan", null, "Executive Producer", PeopleType.CREW, personOrder = 0)
        val cast1 = PeopleEntity(artworkId, 1, "c1", "Edward Norton", "/p.jpg", "The Narrator", PeopleType.CAST, personOrder = 1)
        val cast0 = PeopleEntity(artworkId, 3, "c3", "Meat Loaf", "/p2.jpg", "Robert Paulson", PeopleType.CAST, personOrder = 0)
        every { dao.flowPeople(artworkId) } returns MutableStateFlow(listOf(crew, cast1, cast0))

        // When / Then
        repository.flowPeople(artworkId).test {
            val result = awaitItem()
            // CAST (ordinal 0) first, ordered by personOrder, then CREW (ordinal 1)
            result.map { it.creditId } shouldBe listOf("c3", "c1", "c2")
            result[0].type shouldBe PeopleType.CAST
            result[2].type shouldBe PeopleType.CREW
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("flowPeople should only return people of the requested artwork") {
        // Given : the DAO applies the WHERE artworkId = :artworkId clause
        val other = PeopleEntity(artworkId = 999L, id = 5, creditId = "x1", name = "Other", profilePath = null, role = "Role", type = PeopleType.CAST, personOrder = 0)
        val mine = PeopleEntity(artworkId, id = 6, creditId = "y1", name = "Mine", profilePath = null, role = "Role", type = PeopleType.CAST, personOrder = 0)
        val all = listOf(other, mine)
        every { dao.flowPeople(any()) } answers {
            val queriedId = firstArg<Long>()
            MutableStateFlow(all.filter { it.artworkId == queriedId })
        }

        // When / Then
        repository.flowPeople(artworkId).test {
            awaitItem() shouldBe listOf(
                People(id = 6, creditId = "y1", artworkId = artworkId, name = "Mine", profilePath = null, role = "Role", type = PeopleType.CAST, personOrder = 0)
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("savePeople should convert to entities and call insert from the dao") {
        // Given
        val people = listOf(
            People(id = 1, creditId = "c1", artworkId = artworkId, name = "Edward Norton", profilePath = "/p.jpg", role = "The Narrator", type = PeopleType.CAST, personOrder = 0)
        )

        // When
        repository.savePeople(people = people)

        // Then
        coVerify { dao.insert(people.map { it.toEntity() }) }
    }

})