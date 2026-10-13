package com.mskd.flux.features.credits.domain.usecase

import app.cash.turbine.test
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.core.model.artwork.PeopleType
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.credits.fake.FakeCreditsRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class GetCreditsUseCaseTest : FunSpec({

    val movieArtwork = MediaMockups.movieArtwork // ContentType.MOVIE
    val showArtwork = MediaMockups.showArtwork // ContentType.SHOW

    fun castMember(artworkId: Long, id: Int = 1, name: String = "Edward Norton") = People(
        id = id,
        creditId = "credit-$id",
        artworkId = artworkId,
        name = name,
        profilePath = "/profile.jpg",
        role = "The Narrator",
        type = PeopleType.CAST,
        personOrder = 0
    )

    fun crewMember(artworkId: Long, id: Int = 2, name: String = "Arnon Milchan") = People(
        id = id,
        creditId = "credit-$id",
        artworkId = artworkId,
        name = name,
        profilePath = "/profile.jpg",
        role = "Executive Producer",
        type = PeopleType.CREW,
        personOrder = 0
    )

    test("emits database content and never calls the API when the database has data") {
        // Given
        val cached = castMember(artworkId = movieArtwork.id)
        val castRepository = FakeCreditsRepository(initial = listOf(cached))
        val apiRepository = mockk<ApiRepository>()
        val useCase = GetCreditsUseCase(
            creditsRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then
        useCase(artworkId = movieArtwork.id).test {
            awaitItem() shouldBe persistentListOf(cached)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { apiRepository.getMovieCredits(any()) }
        coVerify(exactly = 0) { apiRepository.getShowCredits(any()) }
    }

    test("fetches movie credits and saves them when the database is empty") {
        // Given
        val expected = listOf(castMember(movieArtwork.id), crewMember(movieArtwork.id, id = 9))
        val castRepository = FakeCreditsRepository()
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getMovieCredits(artworkId = movieArtwork.id) } returns expected
        val useCase = GetCreditsUseCase(
            creditsRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then : first emission is the empty database, second one after the save
        useCase(artworkId = movieArtwork.id).test {
            awaitItem() shouldBe persistentListOf()
            awaitItem() shouldBe expected.toImmutableList()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { apiRepository.getMovieCredits(artworkId = movieArtwork.id) }
        castRepository.savedPeople shouldBe listOf(expected)
    }

    test("fetches show credits when the database is empty") {
        // Given
        val expected = listOf(castMember(showArtwork.id, id = 82055, name = "Junko Takeuchi"))
        val castRepository = FakeCreditsRepository()
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getShowCredits(artworkId = showArtwork.id) } returns expected
        val useCase = GetCreditsUseCase(
            creditsRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then
        useCase(artworkId = showArtwork.id).test {
            awaitItem() shouldBe persistentListOf()
            awaitItem() shouldBe expected.toImmutableList()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { apiRepository.getShowCredits(artworkId = showArtwork.id) }
        coVerify(exactly = 0) { apiRepository.getMovieCredits(any()) }
    }

    test("emits an empty list and does not save when the API returns nothing") {
        // Given
        val castRepository = FakeCreditsRepository()
        val apiRepository = mockk<ApiRepository>()
        coEvery { apiRepository.getMovieCredits(artworkId = movieArtwork.id) } returns emptyList()
        val useCase = GetCreditsUseCase(
            creditsRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then : the database stays empty so only one emission happens
        useCase(artworkId = movieArtwork.id).test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { apiRepository.getMovieCredits(artworkId = movieArtwork.id) }
        castRepository.savedPeople shouldBe emptyList()
    }

    test("emits an empty list and never calls the API when the artwork is unknown") {
        // Given
        val castRepository = FakeCreditsRepository()
        val apiRepository = mockk<ApiRepository>()
        val useCase = GetCreditsUseCase(
            creditsRepository = castRepository,
            apiRepository = apiRepository,
            databaseRepository = FakeDatabaseRepository()
        )

        // When / Then
        useCase(artworkId = -999L).test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { apiRepository.getMovieCredits(any()) }
        coVerify(exactly = 0) { apiRepository.getShowCredits(any()) }
    }

    test("emits the cast and crew merged into a single list") {
        // Given
        val cast = castMember(movieArtwork.id)
        val crew = crewMember(movieArtwork.id, id = 77)
        val castRepository = FakeCreditsRepository(initial = listOf(cast, crew))
        val apiRepository = mockk<ApiRepository>()
        val useCase = GetCreditsUseCase(
            creditsRepository = castRepository,
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