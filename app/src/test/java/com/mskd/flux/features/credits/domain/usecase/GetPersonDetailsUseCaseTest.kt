package com.mskd.flux.features.credits.domain.usecase

import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.core.network.tmdb.domain.model.PersonInfo
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.credits.fake.FakeCreditsRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class GetPersonDetailsUseCaseTest : FunSpec({

    val movieArtwork = MediaMockups.movieArtwork // ContentType.MOVIE
    val showArtwork = MediaMockups.showArtwork // ContentType.SHOW

    fun castMember(artworkId: Long, id: Int = 1, name: String = "Edward Norton") = Person(
        id = id,
        creditId = "credit-$id-$artworkId",
        artworkId = artworkId,
        name = name,
        profilePath = "/profile.jpg",
        role = "The Narrator",
        type = PersonType.CAST,
        personOrder = 0
    )

    fun personInfo(id: Int) = PersonInfo(
        id = id,
        name = "Junko Takeuchi",
        profilePath = "/enriched.jpg",
        birthday = "1972-04-05",
        deathday = null,
        placeOfBirth = "Saitama, Japan",
        biography = "A Japanese voice actress.",
        knownForDepartment = "Acting",
        alsoKnownAs = listOf("Takeuchi Junko"),
        homepage = "https://www.oginext.com/women/takeuchi.html",
        imdbId = "nm0847594",
    )

    test("enriches the details with the API data and the local artworks") {
        // Given
        val person = castMember(artworkId = movieArtwork.id, id = 82055)
        val creditsRepository = FakeCreditsRepository(initial = listOf(person))
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getPersonInfo(personId = person.id) } returns personInfo(id = person.id)
        val useCase = GetPersonDetailsUseCase(
            creditsRepository = creditsRepository,
            databaseRepository = FakeDatabaseRepository(),
            apiRepository = apiRepository
        )

        // When
        val result = useCase(person = person)

        // Then
        result.id shouldBe person.id
        result.name shouldBe "Junko Takeuchi"
        result.profilePath shouldBe "/enriched.jpg"
        result.birthday shouldBe "1972-04-05"
        result.deathday shouldBe null
        result.placeOfBirth shouldBe "Saitama, Japan"
        result.biography shouldBe "A Japanese voice actress."
        result.knownForDepartment shouldBe "Acting"
        result.alsoKnownAs shouldContainExactly listOf("Takeuchi Junko")
        result.homepage shouldBe "https://www.oginext.com/women/takeuchi.html"
        result.imdbId shouldBe "nm0847594"
        result.artworks shouldContainExactly listOf(movieArtwork)

        coVerify(exactly = 1) { apiRepository.getPersonInfo(personId = person.id) }
    }

    test("falls back to the basic Person data when the API fails") {
        // Given
        val person = castMember(artworkId = movieArtwork.id, id = 42)
        val creditsRepository = FakeCreditsRepository(initial = listOf(person))
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getPersonInfo(personId = person.id) } throws RuntimeException("offline")
        val useCase = GetPersonDetailsUseCase(
            creditsRepository = creditsRepository,
            databaseRepository = FakeDatabaseRepository(),
            apiRepository = apiRepository
        )

        // When
        val result = useCase(person = person)

        // Then : only the local data is available, no crash
        result.name shouldBe person.name
        result.profilePath shouldBe person.profilePath
        result.birthday shouldBe null
        result.deathday shouldBe null
        result.placeOfBirth shouldBe null
        result.biography shouldBe null
        result.knownForDepartment shouldBe null
        result.alsoKnownAs shouldBe emptyList()
        result.homepage shouldBe null
        result.imdbId shouldBe null
        result.artworks shouldContainExactly listOf(movieArtwork)
    }

    test("falls back to the basic People data when the API returns nothing") {
        // Given
        val people = castMember(artworkId = movieArtwork.id, id = 7)
        val creditsRepository = FakeCreditsRepository(initial = listOf(people))
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getPersonInfo(personId = people.id) } returns null
        val useCase = GetPersonDetailsUseCase(
            creditsRepository = creditsRepository,
            databaseRepository = FakeDatabaseRepository(),
            apiRepository = apiRepository
        )

        // When
        val result = useCase(people = people)

        // Then
        result.name shouldBe people.name
        result.biography shouldBe null
        result.artworks shouldContainExactly listOf(movieArtwork)
    }

    test("merges the artworks of every artwork the person appears in") {
        // Given : the same person (id) credited on a movie and a show
        val movieCredit = castMember(artworkId = movieArtwork.id, id = 555)
        val showCredit = movieCredit.copy(
            creditId = "credit-555-${showArtwork.id}",
            artworkId = showArtwork.id,
            role = "Director"
        )
        val creditsRepository = FakeCreditsRepository(initial = listOf(movieCredit, showCredit))
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getPersonInfo(personId = movieCredit.id) } returns null
        val useCase = GetPersonDetailsUseCase(
            creditsRepository = creditsRepository,
            databaseRepository = FakeDatabaseRepository(),
            apiRepository = apiRepository
        )

        // When
        val result = useCase(person = movieCredit)

        // Then
        result.artworks shouldHaveSize 2
        result.artworks shouldContainExactly listOf(movieArtwork, showArtwork)
    }

})
