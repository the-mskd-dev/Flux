package com.mskd.flux.features.history.domain.usecase

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.mock.FakeHistoryRepository
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetHistoryUseCaseTest : FunSpec({

    fluxExtensions()

    fun media(path: String, source: FileSource) = MediaMockups.episode1.copy(
        file = UserFile(
            name = "Media name",
            path = path,
            source = source
        )
    )

    fun entry(path: String, timestamp: Long, source: FileSource) = HistoryEntry(
        media = media(path, source),
        timestamp = timestamp,
        title = "Artwork title"
    )

    val t1 = 1L
    val t2 = 2L
    val t3 = 3L

    fun useCase(
        repository: com.mskd.flux.features.history.domain.repository.HistoryRepository,
        sourcesUseCase: FlowSourcesUseCase
    ) = GetHistoryUseCase(repository, sourcesUseCase)

    fun fakeSourcesUseCase(folders: List<UserFolder>): FlowSourcesUseCase = fakeSourcesUseCase(folders)

    fun fakeSourcesUseCase(foldersFlow: Flow<List<UserFolder>>): FlowSourcesUseCase {
        val sourcesUseCase = mockk<FlowSourcesUseCase>()
        every { sourcesUseCase() } returns foldersFlow
        return sourcesUseCase
    }

    test("filters out entries whose file is unavailable, delegating to isAvailableFor") {
        // Given: LOCAL file (always available), a SAF file under an available folder,
        // and a SAF file under an unavailable folder
        val local = entry("/local/a.mp4", t1, FileSource.LOCAL)
        val safAvailable = entry("/tree/movies/b.mkv", t3, FileSource.SAF)
        val safUnavailable = entry("/tree/shows/c.mkv", t2, FileSource.SAF)
        val repository = FakeHistoryRepository(listOf(local, safAvailable, safUnavailable))
        val sourcesUseCase = fakeSourcesUseCase(
            folders = listOf(
                UserFolder(path = "/tree/movies", source = FileSource.SAF, isAvailable = true),
                UserFolder(path = "/tree/shows", source = FileSource.SAF, isAvailable = false)
            )
        )

        // When / Then: only available entries remain, sorted by timestamp descending
        useCase(repository, sourcesUseCase)().test {
            awaitItem() shouldBe persistentListOf(safAvailable, local)
        }
    }

    test("filters out a SAF file when no folder path matches it") {
        val orphanSafEntry = entry("/tree/unknown/a.mkv", t1, FileSource.SAF)
        val repository = FakeHistoryRepository(listOf(orphanSafEntry))
        val sourcesUseCase = fakeSourcesUseCase(
            folders = listOf(UserFolder(path = "/tree/movies", source = FileSource.SAF, isAvailable = true))
        )

        useCase(repository, sourcesUseCase)().test {
            awaitItem() shouldBe persistentListOf()
        }
    }

    test("returns an empty list when the repository has no entries") {
        val repository = FakeHistoryRepository(emptyList())
        val sourcesUseCase = fakeSourcesUseCase(folders = emptyList())

        useCase(repository, sourcesUseCase)().test {
            awaitItem() shouldBe persistentListOf()
        }
    }

    test("sorts remaining entries by timestamp descending, independent of repository order") {
        // Given: entries deliberately out of chronological order, all LOCAL (always available)
        val oldest = entry("/a.mp4", t1, FileSource.LOCAL)
        val newest = entry("/b.mp4", t3, FileSource.LOCAL)
        val middle = entry("/c.mp4", t2, FileSource.LOCAL)
        val repository = FakeHistoryRepository(listOf(oldest, newest, middle))
        val sourcesUseCase = fakeSourcesUseCase(folders = emptyList())

        useCase(repository, sourcesUseCase)().test {
            awaitItem() shouldBe persistentListOf(newest, middle, oldest)
        }
    }

    test("keeps the repository's relative order for equal timestamps (stable sort)") {
        val first = entry("/a.mp4", t1, FileSource.LOCAL)
        val second = entry("/b.mp4", t1, FileSource.LOCAL) // same timestamp as first
        val repository = FakeHistoryRepository(listOf(first, second))
        val sourcesUseCase = fakeSourcesUseCase(folders = emptyList())

        useCase(repository, sourcesUseCase)().test {
            awaitItem() shouldBe persistentListOf(first, second)
        }
    }

    test("reacts to a folder availability change with no repository change") {
        // Given: a SAF entry under a folder that starts available
        val safEntry = entry("/tree/movies/a.mkv", t1, FileSource.SAF)
        val repository = FakeHistoryRepository(listOf(safEntry))
        val foldersFlow = MutableStateFlow(
            listOf(UserFolder(path = "/tree/movies", source = FileSource.SAF, isAvailable = true))
        )
        val sourcesUseCase = fakeSourcesUseCase(foldersFlow)

        useCase(repository, sourcesUseCase)().test {
            awaitItem() shouldBe persistentListOf(safEntry)

            // When: the folder's source gets unmounted (e.g. USB drive removed)
            foldersFlow.value = listOf(
                UserFolder(path = "/tree/movies", source = FileSource.SAF, isAvailable = false)
            )

            // Then: the entry is filtered out without any repository change
            awaitItem() shouldBe persistentListOf()
        }
    }

    test("reacts to a new repository entry, filtered against the current folders state") {
        val existingEntry = entry("/local/a.mp4", t1, FileSource.LOCAL)
        val repositoryFlow = MutableStateFlow(listOf(existingEntry))
        val repository = FakeHistoryRepository(repositoryFlow)
        val sourcesUseCase = fakeSourcesUseCase(
            folders = listOf(UserFolder(path = "/tree/movies", source = FileSource.SAF, isAvailable = false))
        )

        useCase(repository, sourcesUseCase)().test {
            awaitItem() shouldBe persistentListOf(existingEntry)

            val newSafEntry = entry("/tree/movies/b.mkv", t2, FileSource.SAF)
            repositoryFlow.value = listOf(existingEntry, newSafEntry)

            // newSafEntry is filtered out (folder unavailable); existingEntry (LOCAL) stays
            awaitItem() shouldBe persistentListOf(existingEntry)
        }
    }
})