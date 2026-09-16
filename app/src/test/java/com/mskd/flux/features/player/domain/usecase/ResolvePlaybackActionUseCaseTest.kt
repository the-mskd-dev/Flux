package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk

class ResolvePlaybackActionUseCaseTest: FunSpec({

    fluxExtensions()

    lateinit var resolvePlaybackAction: ResolvePlaybackActionUseCase
    lateinit var settings: SettingsDataStore

    beforeTest {

        settings = mockk(relaxed = true)

        resolvePlaybackAction = ResolvePlaybackActionUseCase(
            settings = settings
        )

    }


    test("Resolve Playback action") {

        checkAll(
            iterations = 20,
            Arb.boolean(),
            Arb.boolean(),
            Arb.boolean(),
        ) { isAvailable, externalPlayerIsEnabled, forceInternal ->

            // Given
            val expectedExternalPlayer = externalPlayerIsEnabled && !forceInternal
            val media = MediaMockups.episode1.copy(isAvailable = isAvailable)
            coEvery { settings.externalPlayerIsEnabled() } returns externalPlayerIsEnabled

            // When
            val result = resolvePlaybackAction(media = media, forceInternal = forceInternal)

            // Then
            if (!isAvailable) {
                result.shouldBeInstanceOf<PlaybackAction.Unavailable>()
            } else {
                result.shouldBeInstanceOf<PlaybackAction.OpenPlayer>()
                result.media shouldBe media
                result.externalPlayer shouldBe expectedExternalPlayer
            }

        }

    }

})