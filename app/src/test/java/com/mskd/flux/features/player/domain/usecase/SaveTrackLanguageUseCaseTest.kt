package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.PlayerMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

class SaveTrackLanguageUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var settings: SettingsDataStore
    lateinit var saveTrackLanguage: SaveTrackLanguageUseCase

    beforeTest {

        settings = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(
                SettingsDataStore.State(
                    subtitlesLanguage = Locale.ENGLISH,
                    audioLanguage = Locale.FRENCH
                )
            )
        }

        saveTrackLanguage = SaveTrackLanguageUseCase(
            settings = settings
        )

    }

    test("save subtitles language") {

        // When
        saveTrackLanguage(track = PlayerMockups.Subtitles.english)

        // Then
        coVerify { settings.setSubtitlesLanguage(Locale.forLanguageTag("en")) }
        coVerify(exactly = 0) { settings.setAudioLanguage(any()) }

    }

    test("save audio language") {

        // When
        saveTrackLanguage(track = PlayerMockups.Audio.french)

        // Then
        coVerify { settings.setAudioLanguage(Locale.forLanguageTag("fr")) }
        coVerify(exactly = 0) { settings.setSubtitlesLanguage(any()) }

    }

    test("no subtitles track does not save anything") {

        // When
        saveTrackLanguage(track = PlayerTrack.NO_SUBTITLES)

        // Then
        coVerify(exactly = 0) { settings.setSubtitlesLanguage(any()) }
        coVerify(exactly = 0) { settings.setAudioLanguage(any()) }

    }

    test("store failure does not throw") {

        // Given
        coEvery { settings.setAudioLanguage(any()) } throws RuntimeException("Mock database write failure")

        // When - Should catch and not throw
        saveTrackLanguage(track = PlayerMockups.Audio.english)

    }

    test("get subtitles language returns saved locale") {
        saveTrackLanguage.getSubtitlesLanguage() shouldBe Locale.ENGLISH
    }

    test("get audio language returns saved locale") {
        saveTrackLanguage.getAudioLanguage() shouldBe Locale.FRENCH
    }

})