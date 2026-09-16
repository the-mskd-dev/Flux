package com.mskd.flux.features.history.domain.usecase

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.history.mock.FakeHistoryRepository
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.features.sources.fake.FakeSourcesRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

class GetHistoryUseCaseTest : FunSpec({

    fluxExtensions()

    fun media(path: String) = MediaMockups.episode1.copy(
        file = UserFile(
            name = "Media name",
            path = path,
            source = FileSource.SAF
        )
    )

    fun entry(path: String, timestamp: Long) = HistoryEntry(
        media = media(path),
        timestamp = timestamp,
        title = "Artwork title"
    )

    val t1 = 1L
    val t2 = 2L
    val t3 = 3L

    fun createUseCase(
        historyRepository: HistoryRepository,
        userFolders: List<UserFolder> = emptyList()
    ): GetHistoryUseCase {
        val sourcesRepository = FakeSourcesRepository(userFolders)
        val sourcesUseCase = FlowSourcesUseCase(sourcesRepository)

        return GetHistoryUseCase(
            repository = historyRepository,
            sourcesUseCase = sourcesUseCase
        )
    }

    test("emits only entries with existing file in sources") {
        // Given
        val availableFolder = UserFolder(
            path = "/sdcard/movies",
            source = FileSource.SAF,
            isAvailable = true
        )
        val entryA = entry("/sdcard/movies/a.mp4", t1)
        val entryB = entry("/sdcard/other/b.mp4", t2) // Unavailable

        val repository = FakeHistoryRepository(listOf(entryA, entryB))
        val useCase = createUseCase(repository, userFolders = listOf(availableFolder))

        // When / Then
        useCase().test {
            awaitItem() shouldBe persistentListOf(entryA)
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("empty list when no sources available") {
        // Given
        val entryA = entry("/a.mp4", t1)
        val repository = FakeHistoryRepository(listOf(entryA))
        val useCase = createUseCase(repository, userFolders = emptyList())

        // When / Then
        useCase().test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("empty list when repository is empty") {
        // Given
        val availableFolder = UserFolder(path = "/", source = FileSource.SAF, isAvailable = true)
        val repository = FakeHistoryRepository(emptyList())
        val useCase = createUseCase(repository, userFolders = listOf(availableFolder))

        // When / Then
        useCase().test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("returns sorted list according to timestamps descending") {
        // Given
        val availableFolder = UserFolder(path = "/", source = FileSource.SAF, isAvailable = true)
        val entryA = entry("/a.mp4", t1)
        val entryB = entry("/b.mp4", t3) // Most recent
        val entryC = entry("/c.mp4", t2)

        // unsorted entries from FakeHistoryRepository
        val repository = FakeHistoryRepository(listOf(entryA, entryB, entryC))
        val useCase = createUseCase(repository, userFolders = listOf(availableFolder))

        // When / Then (expected order : B (t3), C (t2), A (t1))
        useCase().test {
            awaitItem() shouldBe persistentListOf(entryB, entryC, entryA)
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("new emit when history repository emits a new value") {
        // Given
        val availableFolder = UserFolder(path = "/", source = FileSource.SAF, isAvailable = true)
        val entryA = entry("/a.mp4", t1)
        val entryB = entry("/b.mp4", t2)

        val repositoryFlow = MutableStateFlow(listOf(entryA))
        val repository = FakeHistoryRepository(repositoryFlow)
        val useCase = createUseCase(repository, userFolders = listOf(availableFolder))

        // When / Then
        useCase().test {
            // First emit
            awaitItem() shouldBe persistentListOf(entryA)

            // Repository update
            repositoryFlow.value = listOf(entryA, entryB)

            // Second emit, sorted by timestamp (entryB in first)
            awaitItem() shouldBe persistentListOf(entryB, entryA)

            cancelAndIgnoreRemainingEvents()
        }
    }
})