package com.mskd.flux.features.player.data.manager

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.session.MediaController
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.MediaInfo
import com.mskd.flux.features.player.domain.model.SubtitleCue
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

    fun setReady(player: Player = mockk(relaxed = true)) {
        val stateField = AndroidPlayerManager::class.java.getDeclaredField("_state").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (stateField.get(manager) as MutableStateFlow<PlayerManager.State<Player>>).value = PlayerManager.State.Ready(player = player)
    }

    fun trackGroup(format: Format, selected: Boolean = true) = Tracks.Group(
        TrackGroup(format),
        false,
        intArrayOf(C.FORMAT_HANDLED),
        booleanArrayOf(selected)
    )

    suspend fun mediaInfoOf(vararg groups: Tracks.Group): MediaInfo? {
        manager.onTracksChanged(Tracks(groups.toList()))
        return (manager.flow.first() as PlayerManager.State.Ready).mediaInfo
    }

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

    test("cues reach the subtitles flow as platform-independent cues, with their placement") {
        val sign = Cue.Builder()
            .setText("Translated sign")
            .setLine(0.05f, Cue.LINE_TYPE_FRACTION)
            .setLineAnchor(Cue.ANCHOR_TYPE_START)
            .build()

        manager.onCues(CueGroup(listOf(sign), 0L))

        manager.subtitles.first() shouldBe listOf(
            SubtitleCue(text = "Translated sign", line = 0.05f, lineAnchor = CueAnchor.START)
        )
    }

    test("subtitle delay is sent to the service and exposed in state") {
        val controller = mockk<MediaController>(relaxed = true)
        setReady(controller)

        manager.setSubtitleDelay(delayMs = -300L)

        verify { controller.sendCustomCommand(any(), any()) }
        (manager.flow.first() as PlayerManager.State.Ready).subtitleDelayMs shouldBe -300L
    }

    test("chosen speed reaches the player and is exposed in state") {
        val player = mockk<Player>(relaxed = true)
        setReady(player)

        manager.setPlaybackSpeed(1.75f)

        verify { player.setPlaybackSpeed(1.75f) }
        (manager.flow.first() as PlayerManager.State.Ready).playbackSpeed shouldBe 1.75f
    }

    test("media info maps the selected video and audio tracks of a typical file") {
        setReady()

        val video = Format.Builder()
            .setContainerMimeType(MimeTypes.VIDEO_MATROSKA)
            .setSampleMimeType(MimeTypes.VIDEO_H264)
            .setWidth(1920)
            .setHeight(1080)
            .setFrameRate(23.976f)
            .setAverageBitrate(4_500_000)
            .build()
        val audio = Format.Builder()
            .setSampleMimeType(MimeTypes.AUDIO_E_AC3)
            .setChannelCount(6)
            .setSampleRate(48_000)
            .build()

        mediaInfoOf(trackGroup(video), trackGroup(audio)) shouldBe MediaInfo(
            container = "MKV",
            videoCodec = "H.264",
            width = 1920,
            height = 1080,
            frameRate = 23.976f,
            bitrate = 4_500_000,
            audioCodec = "E-AC-3",
            channels = 6,
            sampleRate = 48_000,
        )
    }

    test("media info describes the selected video track, not the first one") {
        setReady()

        val unselectedVideo = Format.Builder().setSampleMimeType(MimeTypes.VIDEO_H265).build()
        val selectedVideo = Format.Builder().setSampleMimeType(MimeTypes.VIDEO_H264).build()

        mediaInfoOf(
            trackGroup(unselectedVideo, selected = false),
            trackGroup(selectedVideo),
        ) shouldBe MediaInfo(videoCodec = "H.264")
    }

    test("unknown values are null, never 0 or a raw sentinel") {
        setReady()

        // Only the codec is known: Media3 reports the other values as NO_VALUE (-1)
        val video = Format.Builder().setSampleMimeType(MimeTypes.VIDEO_H264).build()

        mediaInfoOf(trackGroup(video)) shouldBe MediaInfo(videoCodec = "H.264")
    }
})
