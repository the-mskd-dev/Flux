package com.mskd.flux.features.player.data.manager

import androidx.media3.common.Player
import com.mskd.flux.configs.fluxExtensions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import com.mskd.flux.features.player.domain.manager.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
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

    test("chosen speed reaches the player and is exposed in state") {
        val player = mockk<Player>(relaxed = true)
        val stateField = AndroidPlayerManager::class.java.getDeclaredField("_state").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (stateField.get(manager) as MutableStateFlow<PlayerManager.State<Player>>).value =
            PlayerManager.State.Ready(player = player)

        manager.setPlaybackSpeed(1.75f)

        verify { player.setPlaybackSpeed(1.75f) }
        (manager.flow.first() as PlayerManager.State.Ready).playbackSpeed shouldBe 1.75f
    }
})
