package com.mskd.flux.features.people.domain.usecase

import app.cash.turbine.test
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.people.fake.FakePeopleRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class GetPeopleUseCaseTest : FunSpec({

    val movieArtwork = MediaMockups.movieArtwork // ContentType.MOVIE
    val showArtwork = MediaMockups.showArtwork // ContentType.SHOW

    fun castMember(artworkId: Long, id: Int = 1, name: String = "Edward Norton") = Person(
        id = id,
        creditId = "credit-$id",
        artworkId = artworkId,
        name = name,
        profilePath = "/profile.jpg",
        role = "The Narrator",
        type = PersonType.CAST,
        personOrder = 0
    )

    fun crewMember(artworkId: Long, id: Int = 2, name: String = "Arnon Milchan") = Person(
        id = id,
        creditId = "credit-$id",
        artworkId = artworkId,
        name = name,
        profilePath = "/profile.jpg",
        role = "Executive Producer",
        type = PersonType.CREW,
        personOrder = 0
    )

    test("emits database content and never calls the API when the database has data") {
        // Given
        val cached = castMember(artworkId = movieArtwork.id)
        val castRepository = FakePeopleRepository(initial = listOf(cached))
        val apiRepository = mockk<ApiRepository>()
        val useCase = GetPeopleUseCase(
            peopleRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then
        useCase(artworkId = movieArtwork.id).test {
            awaitItem() shouldBe persistentListOf(cached)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { apiRepository.getMoviePeople(any()) }
        coVerify(exactly = 0) { apiRepository.getShowPeople(any()) }
    }

    test("fetches movie people and saves them when the database is empty") {
        // Given
        val expected = listOf(castMember(movieArtwork.id), crewMember(movieArtwork.id, id = 9))
        val castRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getMoviePeople(artworkId = movieArtwork.id) } returns expected
        val useCase = GetPeopleUseCase(
            peopleRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then : first emission is the empty database, second one after the save
        useCase(artworkId = movieArtwork.id).test {
            awaitItem() shouldBe persistentListOf()
            awaitItem() shouldBe expected.toImmutableList()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { apiRepository.getMoviePeople(artworkId = movieArtwork.id) }
        castRepository.savedPeople shouldBe listOf(expected)
    }

    test("fetches show people when the database is empty") {
        // Given
        val expected = listOf(castMember(showArtwork.id, id = 82055, name = "Junko Takeuchi"))
        val castRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getShowPeople(artworkId = showArtwork.id) } returns expected
        val useCase = GetPeopleUseCase(
            peopleRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then
        useCase(artworkId = showArtwork.id).test {
            awaitItem() shouldBe persistentListOf()
            awaitItem() shouldBe expected.toImmutableList()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { apiRepository.getShowPeople(artworkId = showArtwork.id) }
        coVerify(exactly = 0) { apiRepository.getMoviePeople(any()) }
    }

    test("emits an empty list and does not save when the API returns nothing") {
        // Given
        val castRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getMoviePeople(artworkId = movieArtwork.id) } returns emptyList()
        val useCase = GetPeopleUseCase(
            peopleRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then : the database stays empty so only one emission happens
        useCase(artworkId = movieArtwork.id).test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { apiRepository.getMoviePeople(artworkId = movieArtwork.id) }
        castRepository.savedPeople shouldBe emptyList()
    }

    test("emits an empty list and never calls the API when the artwork is unknown") {
        // Given
        val castRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        val useCase = GetPeopleUseCase(
            peopleRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then
        useCase(artworkId = -999L).test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { apiRepository.getMoviePeople(any()) }
        coVerify(exactly = 0) { apiRepository.getShowPeople(any()) }
    }

    test("emits the cast and crew merged into a single list") {
        // Given
        val cast = castMember(movieArtwork.id)
        val crew = crewMember(movieArtwork.id, id = 77)
        val castRepository = FakePeopleRepository(initial = listOf(cast, crew))
        val apiRepository = mockk<ApiRepository>()
        val useCase = GetPeopleUseCase(
            peopleRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then
        useCase(artworkId = movieArtwork.id).test {
            val result = awaitItem()
            result shouldHaveSize 2
            result shouldBe listOf(cast, crew).toImmutableList()
            cancelAndIgnoreRemainingEvents()
        }
    }

})