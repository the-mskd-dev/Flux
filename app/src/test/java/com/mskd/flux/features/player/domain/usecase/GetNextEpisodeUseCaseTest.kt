package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class GetNextEpisodeUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var database: DatabaseRepository
    lateinit var useCase: GetNextEpisodeUseCase

    beforeTest {
        database = mockk(relaxed = true)
        useCase = GetNextEpisodeUseCase(database = database)
    }

    test("returns null when media is null") {
        useCase(media = null).shouldBeNull()
    }

    test("returns null when media is a Movie") {
        useCase(media = MediaMockups.movie).shouldBeNull()
    }

    test("returns null when episode is unknown") {
        useCase(media = MediaMockups.unknownEpisode).shouldBeNull()
    }

    test("returns next episode when one exists") {

        // Given
        coEvery { database.getNextEpisodeFor(episode = MediaMockups.episode1) } returns MediaMockups.episode2

        // When
        val nextEpisode = useCase(media = MediaMockups.episode1)

        // Then
        nextEpisode shouldBe MediaMockups.episode2

    }

    test("returns null when the next episode is unavailable") {

        // Given
        val unavailableEpisode2 = MediaMockups.episode2.copy(isAvailable = false)
        coEvery { database.getNextEpisodeFor(episode = MediaMockups.episode1) } returns unavailableEpisode2

        // When
        val nextEpisode = useCase(media = MediaMockups.episode1)

        // Then
        nextEpisode.shouldBeNull()

    }

    test("does not skip to a later available episode when the next one is unavailable") {

        // Given
        val unavailableEpisode2 = MediaMockups.episode2.copy(isAvailable = false)
        coEvery { database.getNextEpisodeFor(episode = MediaMockups.episode1) } returns unavailableEpisode2
        coEvery { database.getNextEpisodeFor(episode = unavailableEpisode2) } returns MediaMockups.episode3

        // When
        val nextEpisode = useCase(media = MediaMockups.episode1)

        // Then
        nextEpisode.shouldBeNull()

    }

    test("returns null when no next episode exists (last episode)") {

        // Given
        coEvery { database.getNextEpisodeFor(episode = MediaMockups.episode3) } returns null

        // When
        val nextEpisode = useCase(media = MediaMockups.episode3)

        // Then
        nextEpisode.shouldBeNull()

    }

})