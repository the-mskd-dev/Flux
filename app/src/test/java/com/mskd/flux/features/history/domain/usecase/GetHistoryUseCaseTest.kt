package com.mskd.flux.features.history.domain.usecase

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.history.mock.FakeHistoryRepository
import com.mskd.flux.features.sources.domain.model.Source // Remplace par ton vrai modèle Source
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class GetHistoryUseCaseTest : FunSpec({

    // Configure le Dispatchers.Main pour les tests de coroutines
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

    // Helper pour créer le UseCase avec un mock ou fake de sourcesUseCase
    fun createUseCase(
        repository: HistoryRepository,
        sourcesUseCase: FlowSourcesUseCase
    ): GetHistoryUseCase {
        return GetHistoryUseCase(
            repository = repository,
            sourcesUseCase = sourcesUseCase
        )
    }

    test("emits only entries with available sources") {
        // Given
        val entryA = entry("/sdcard/movies/a.mp4", t1)
        val entryB = entry("/sdcard/movies/b.mp4", t2)
        val repository = FakeHistoryRepository(listOf(entryA, entryB))

        // On mock FlowSourcesUseCase pour renvoyer les sources valides pour entryA seulement
        val sourcesUseCase = mockk<FlowSourcesUseCase>()
        val mockSources = listOf<UserFolder>(/* mets tes objets Source valides ici */)
        every { sourcesUseCase() } returns flowOf(mockSources)

        // Note: Assure-toi que mockSources fait en sorte que `mockSources.isAvailableFor(entryA.media.file)` soit true
        // et `mockSources.isAvailableFor(entryB.media.file)` soit false.

        val useCase = createUseCase(repository, sourcesUseCase)

        // When / Then
        useCase().test {
            awaitItem() shouldBe persistentListOf(entryA)
            cancelAndIgnoreRemainingEvents() // Bonne pratique avec les StateFlow/Combine
        }
    }

    test("empty list when no sources available") {
        // Given
        val entryA = entry("/a.mp4", t1)
        val repository = FakeHistoryRepository(listOf(entryA))

        val sourcesUseCase = mockk<FlowSourcesUseCase>()
        every { sourcesUseCase() } returns flowOf(emptyList()) // Aucune source disponible

        val useCase = createUseCase(repository, sourcesUseCase)

        // When / Then
        useCase().test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("empty list when repository is empty") {
        // Given
        val repository = FakeHistoryRepository(emptyList())
        val sourcesUseCase = mockk<FlowSourcesUseCase>()
        every { sourcesUseCase() } returns flowOf(listOf(/* des sources */))

        val useCase = createUseCase(repository, sourcesUseCase)

        // When / Then
        useCase().test {
            awaitItem() shouldBe persistentListOf()
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("returns sorted list according to timestamps descending") {
        // Given
        val entryA = entry("/a.mp4", t1)
        val entryB = entry("/b.mp4", t3) // Plus récent
        val entryC = entry("/c.mp4", t2)

        val repository = FakeHistoryRepository(listOf(entryA, entryB, entryC))
        val sourcesUseCase = mockk<FlowSourcesUseCase>()
        every { sourcesUseCase() } returns flowOf(listOf(/* sources qui valident A, B et C */))

        val useCase = createUseCase(repository, sourcesUseCase)

        // When / Then (Ordre attendu : B(t3), C(t2), A(t1))
        useCase().test {
            awaitItem() shouldBe persistentListOf(entryB, entryC, entryA)
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("new emit when repository emits a new value") {
        // Given
        val entryA = entry("/a.mp4", t1)
        val entryB = entry("/b.mp4", t2)
        val repositoryFlow = MutableStateFlow(listOf(entryA))
        val repository = FakeHistoryRepository(repositoryFlow)

        val sourcesUseCase = mockk<FlowSourcesUseCase>()
        every { sourcesUseCase() } returns flowOf(listOf(/* sources qui valident A et B */))

        val useCase = createUseCase(repository, sourcesUseCase)

        // When / Then
        useCase().test {
            // Premier émission
            awaitItem() shouldBe persistentListOf(entryA)

            // Mise à jour du repository
            repositoryFlow.value = listOf(entryA, entryB)

            // Seconde émission (t2 > t1 => B puis A)
            awaitItem() shouldBe persistentListOf(entryB, entryA)

            cancelAndIgnoreRemainingEvents()
        }
    }
})