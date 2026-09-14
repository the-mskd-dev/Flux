package com.mskd.flux.features.history.domain.usecase

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.mock.FakeHistoryRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow

class GetHistoryUseCaseTest : FunSpec({

    fluxExtensions()

    fun media(path: String) = MediaMockups.episode1.copy(
        file = UserFile(
            name = "Media name",
            path = path,
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

    test("emits only entries with existing file") {
        // Given
        val entryA = entry("/sdcard/movies/a.mp4", t1)
        val entryB = entry("/sdcard/movies/b.mp4", t2)
        val repository = FakeHistoryRepository(listOf(entryA, entryB))
        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery {
            filterExistingFilesUseCase(files = listOf(entryA.media.file, entryB.media.file))
        } returns listOf(entryA.media.file)

        val useCase = GetHistoryUseCase(repository, filterExistingFilesUseCase)

        // When / Then
        useCase().test {
            awaitItem() shouldBe persistentListOf(entryA)
            awaitComplete()
        }
    }

    test("empty list when no existing file") {
        val entryA = entry("/a.mp4", t1)
        val repository = FakeHistoryRepository(listOf(entryA))
        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery { filterExistingFilesUseCase(files = listOf(entryA.media.file)) } returns emptyList()

        val useCase = GetHistoryUseCase(repository, filterExistingFilesUseCase)

        useCase().test {
            awaitItem() shouldBe persistentListOf()
            awaitComplete()
        }
    }

    test("empty list when repository is empty") {
        val repository = FakeHistoryRepository(emptyList())
        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery { filterExistingFilesUseCase(files = emptyList()) } returns emptyList()

        val useCase = GetHistoryUseCase(repository, filterExistingFilesUseCase)

        useCase().test {
            awaitItem() shouldBe persistentListOf()
            awaitComplete()
        }
    }

    test("returns sorted list according to timestamps") {
        val entryA = entry("/a.mp4", t1)
        val entryB = entry("/b.mp4", t3)
        val entryC = entry("/c.mp4", t2)

        // Fake random order from repository
        val repository = FakeHistoryRepository(listOf(entryA, entryB, entryC))
        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery {
            filterExistingFilesUseCase(
                files = listOf(entryA.media.file, entryB.media.file, entryC.media.file)
            )
        } returns listOf(entryC.media.file, entryA.media.file, entryB.media.file) // ordre différent aussi

        val useCase = GetHistoryUseCase(repository, filterExistingFilesUseCase)

        useCase().test {
            awaitItem() shouldBe persistentListOf(entryB, entryC, entryA)
            awaitComplete()
        }
    }

    test("new emit when repository emits a new value") {
        val entryA = entry("/a.mp4", t1)
        val entryB = entry("/b.mp4", t2)
        val repositoryFlow = MutableStateFlow(listOf(entryA))
        val repository = FakeHistoryRepository(repositoryFlow)
        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery {
            filterExistingFilesUseCase(files = listOf(entryA.media.file))
        } returns listOf(entryA.media.file)
        coEvery {
            filterExistingFilesUseCase(files = listOf(entryA.media.file, entryB.media.file))
        } returns listOf(entryB.media.file)

        val useCase = GetHistoryUseCase(repository, filterExistingFilesUseCase)

        useCase().test {
            awaitItem() shouldBe persistentListOf(entryA)

            repositoryFlow.value = listOf(entryA, entryB)

            awaitItem() shouldBe persistentListOf(entryB)
        }
    }
})