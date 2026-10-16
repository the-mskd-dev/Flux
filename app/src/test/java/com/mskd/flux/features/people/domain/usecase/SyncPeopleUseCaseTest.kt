package com.mskd.flux.features.people.domain.usecase

import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.people.fake.FakePeopleRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class SyncPeopleUseCaseTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()
    lateinit var testScope: TestScope

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    val movieArtwork = MediaMockups.movieArtwork
    val showArtwork = MediaMockups.showArtwork

    fun castMember(artworkId: Long, id: Int = 1, name: String = "Edward Norton") = Person(
        id = id,
        creditId = "credit-$id",
        artworkId = artworkId,
        name = name,
        originalName = "Edward Norton",
        profilePath = "/profile.jpg",
        role = "The Narrator",
        type = PersonType.CAST,
        personOrder = 0,
    )

    fun createUseCase(
        databaseRepository: DatabaseRepository = FakeDatabaseRepository(),
        apiRepository: ApiRepository = mockk(relaxed = true),
        peopleRepository: FakePeopleRepository = FakePeopleRepository(),
        userDataStore: UserDataStore = mockk(relaxed = true),
    ) = SyncPeopleUseCase(
        databaseRepository = databaseRepository,
        apiRepository = apiRepository,
        peopleRepository = peopleRepository,
        userDataStore = userDataStore,
        scope = testScope,
    )

    test("when targeted artworks are provided, syncs only those artworks and does not update sync timestamp") {
        val peopleRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        val userDataStore = mockk<UserDataStore>(relaxed = true)
        val expectedPerson = castMember(artworkId = movieArtwork.id)
        coEvery { apiRepository.getMoviePeople(artworkId = movieArtwork.id) } returns listOf(expectedPerson)

        val useCase = createUseCase(
            apiRepository = apiRepository,
            peopleRepository = peopleRepository,
            userDataStore = userDataStore,
        )

        useCase.invoke(artworks = listOf(movieArtwork))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { apiRepository.getMoviePeople(artworkId = movieArtwork.id) }
        coVerify(exactly = 0) { apiRepository.getShowPeople(any()) }
        coVerify(exactly = 0) { userDataStore.setPeopleSyncTime(any()) }
        peopleRepository.savedPeople.flatten() shouldContainExactly listOf(expectedPerson)
    }

    test("when force is true and artworks is null, syncs all artworks from database and updates sync timestamp") {
        val peopleRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        val userDataStore = mockk<UserDataStore>(relaxed = true)
        val moviePerson = castMember(artworkId = movieArtwork.id, id = 10)
        val showPerson = castMember(artworkId = showArtwork.id, id = 20)
        coEvery { apiRepository.getMoviePeople(artworkId = movieArtwork.id) } returns listOf(moviePerson)
        coEvery { apiRepository.getShowPeople(artworkId = showArtwork.id) } returns listOf(showPerson)

        val useCase = createUseCase(
            databaseRepository = FakeDatabaseRepository(),
            apiRepository = apiRepository,
            peopleRepository = peopleRepository,
            userDataStore = userDataStore,
        )

        useCase.invoke(force = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { apiRepository.getMoviePeople(artworkId = movieArtwork.id) }
        coVerify(exactly = 1) { apiRepository.getShowPeople(artworkId = showArtwork.id) }
        coVerify(exactly = 1) { userDataStore.setPeopleSyncTime(any()) }
        peopleRepository.savedPeople.flatten() shouldContainExactly listOf(moviePerson, showPerson)
    }

    test("when force is false and less than 7 days have elapsed, does not sync") {
        val apiRepository = mockk<ApiRepository>()
        val userDataStore = mockk<UserDataStore>()
        val now = Clock.System.now().toEpochMilliseconds()
        val recentSync = now - 3.days.inWholeMilliseconds // 3 days ago
        coEvery { userDataStore.getPeopleSyncTime() } returns recentSync

        val useCase = createUseCase(
            apiRepository = apiRepository,
            userDataStore = userDataStore,
        )

        useCase.invoke(force = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { apiRepository.getMoviePeople(any()) }
        coVerify(exactly = 0) { apiRepository.getShowPeople(any()) }
        coVerify(exactly = 0) { userDataStore.setPeopleSyncTime(any()) }
    }

    test("when force is false and 7 days or more have elapsed, syncs all artworks and updates timestamp") {
        val peopleRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        val userDataStore = mockk<UserDataStore>(relaxed = true)
        val now = Clock.System.now().toEpochMilliseconds()
        val oldSync = now - 8.days.inWholeMilliseconds // 8 days ago
        coEvery { userDataStore.getPeopleSyncTime() } returns oldSync

        val useCase = createUseCase(
            apiRepository = apiRepository,
            peopleRepository = peopleRepository,
            userDataStore = userDataStore,
        )

        useCase.invoke(force = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { apiRepository.getMoviePeople(any()) }
        coVerify(atLeast = 1) { apiRepository.getShowPeople(any()) }
        coVerify(exactly = 1) { userDataStore.setPeopleSyncTime(any()) }
    }

    test("when sync is run for the first time (timestamp is 0L), syncs all artworks") {
        val apiRepository = mockk<ApiRepository>()
        val userDataStore = mockk<UserDataStore>(relaxed = true)
        coEvery { userDataStore.getPeopleSyncTime() } returns 0L

        val useCase = createUseCase(
            apiRepository = apiRepository,
            userDataStore = userDataStore,
        )

        useCase.invoke(force = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { apiRepository.getMoviePeople(any()) }
        coVerify(atLeast = 1) { apiRepository.getShowPeople(any()) }
        coVerify(exactly = 1) { userDataStore.setPeopleSyncTime(any()) }
    }

    test("ignores artworks with UNKNOWN_ID") {
        val unknownArtwork = movieArtwork.copy(id = Artwork.UNKNOWN_ID)
        val apiRepository = mockk<ApiRepository>()
        val userDataStore = mockk<UserDataStore>(relaxed = true)

        val useCase = createUseCase(
            apiRepository = apiRepository,
            userDataStore = userDataStore,
        )

        useCase.invoke(artworks = listOf(unknownArtwork))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { apiRepository.getMoviePeople(any()) }
        coVerify(exactly = 0) { apiRepository.getShowPeople(any()) }
    }

    test("handles API exception gracefully and continues syncing remaining artworks") {
        val peopleRepository = FakePeopleRepository()
        val apiRepository = mockk<ApiRepository>()
        val userDataStore = mockk<UserDataStore>(relaxed = true)
        val showPerson = castMember(artworkId = showArtwork.id, id = 99)

        coEvery { apiRepository.getMoviePeople(artworkId = movieArtwork.id) } throws RuntimeException("TMDB Network error")
        coEvery { apiRepository.getShowPeople(artworkId = showArtwork.id) } returns listOf(showPerson)

        val useCase = createUseCase(
            apiRepository = apiRepository,
            peopleRepository = peopleRepository,
            userDataStore = userDataStore,
        )

        useCase.invoke(artworks = listOf(movieArtwork, showArtwork))
        testDispatcher.scheduler.advanceUntilIdle()

        // Show person should still be saved despite movie throwing an error
        peopleRepository.savedPeople.flatten() shouldContainExactly listOf(showPerson)
    }

})
