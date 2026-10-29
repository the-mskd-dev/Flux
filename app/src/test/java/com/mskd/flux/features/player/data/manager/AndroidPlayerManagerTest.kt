package com.mskd.flux.features.player.data.manager

import androidx.media3.common.Player
import com.mskd.flux.configs.fluxExtensions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.flow.first

class AndroidPlayerManagerTest : FunSpec({

    fluxExtensions()

    lateinit var manager: AndroidPlayerManager

    beforeTest {
        manager = AndroidPlayerManager(
            context = mockk(relaxed = true),
            saveTrackLanguageUseCase = mockk(relaxed = true),
            decoderManagerHolder = mockk(relaxed = true)
        )
    }

    fun positionAt(positionMs: Long) = Player.PositionInfo(
        null, 0, null, null, 0, positionMs, positionMs, -1, -1
    )

    // Progress is only polled while playing: a seek while paused must still be reflected,
    // otherwise the seek bar snaps back to the pre-seek position once the scrub settles.
    test("seek while paused updates progress") {
        manager.onPositionDiscontinuity(
            positionAt(10_000L),
            positionAt(42_000L),
            Player.DISCONTINUITY_REASON_SEEK
        )

        manager.progress.first() shouldBe 42_000L
    }

    test("non-seek discontinuity does not touch progress") {
        manager.onPositionDiscontinuity(
            positionAt(10_000L),
            positionAt(0L),
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION
        )

        manager.progress.first() shouldBe 0L
    }
})
