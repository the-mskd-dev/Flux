package com.mskd.flux.features.artwork.usecase

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCaseImpl
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class ObserveArtworkUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var database: DatabaseRepository
    lateinit var useCase: ObserveArtworkUseCaseImpl

    beforeTest {
        database = mockk()
        useCase = ObserveArtworkUseCaseImpl(database = database)
    }

    test("observe valid movie returns FullMovie content state") {
        val movieArtwork = MediaMockups.movieArtwork
        val movie = MediaMockups.movie

        every { database.flowArtwork(movieArtwork.id) } returns flowOf(movieArtwork)
        every { database.flowMovie(movieArtwork.id) } returns flowOf(movie)
        every { database.flowSeasons(movieArtwork.id) } returns flowOf(emptyList())
        every { database.flowEpisodes(movieArtwork.id) } returns flowOf(emptyList())

        useCase.flow.test {
            useCase(movieArtwork.id)

            val state = awaitItem()
            state.shouldBeInstanceOf<State.Content<FullArtwork>>()
            val content = state.content
            content.shouldBeInstanceOf<FullArtwork.FullMovie>()
            content.artwork shouldBe movieArtwork
            content.movie shouldBe movie
        }
    }

    test("observe valid show returns FullShow content state") {
        val showArtwork = MediaMockups.showArtwork
        val seasons = MediaMockups.seasons
        val episodes = MediaMockups.episodes

        every { database.flowArtwork(showArtwork.id) } returns flowOf(showArtwork)
        every { database.flowMovie(showArtwork.id) } returns flowOf(null)
        every { database.flowSeasons(showArtwork.id) } returns flowOf(seasons)
        every { database.flowEpisodes(showArtwork.id) } returns flowOf(episodes)

        useCase.flow.test {
            useCase(showArtwork.id)

            val state = awaitItem()
            state.shouldBeInstanceOf<State.Content<FullArtwork>>()
            val content = state.content
            content.shouldBeInstanceOf<FullArtwork.FullShow>()
            content.artwork shouldBe showArtwork
            content.seasons shouldBe seasons.filter { it.season in 1..2 }
            content.episodes shouldBe episodes
        }
    }

    test("observe invalid artwork returns Error state") {
        val invalidId = -999L

        every { database.flowArtwork(invalidId) } returns flowOf(null)
        every { database.flowMovie(invalidId) } returns flowOf(null)
        every { database.flowSeasons(invalidId) } returns flowOf(emptyList())
        every { database.flowEpisodes(invalidId) } returns flowOf(emptyList())

        useCase.flow.test {
            useCase(invalidId)

            val state = awaitItem()
            state.shouldBeInstanceOf<State.Error>()
        }
    }

})
