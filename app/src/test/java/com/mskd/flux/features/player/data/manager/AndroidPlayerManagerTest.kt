package com.mskd.flux.features.player.data.manager

import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.TrackGroup
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.session.MediaController
import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.files.domain.model.ExternalSubtitle
import com.mskd.flux.features.player.domain.manager.PlayerManager
import com.mskd.flux.features.player.domain.model.CueAnchor
import com.mskd.flux.features.player.domain.model.MediaInfo
import com.mskd.flux.features.player.domain.model.SubtitleCue
import com.mskd.flux.features.player.domain.model.SubtitleTextSpan
import com.mskd.flux.features.player.domain.usecase.SaveTrackLanguageUseCase
import com.mskd.flux.features.player.fake.colorSpan
import com.mskd.flux.features.player.fake.spannedText
import com.mskd.flux.features.player.fake.stubSpannedStringValueOf
import com.mskd.flux.features.player.fake.styleSpan
import com.mskd.flux.features.player.fake.underlineSpan
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.external_subtitle
import flux.shared.generated.resources.season_and_episode
import flux.shared.generated.resources.track
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString

class AndroidPlayerManagerTest : FunSpec({

    fluxExtensions()
    stubSpannedStringValueOf()

    lateinit var manager: AndroidPlayerManager
    lateinit var saveTrackLanguageUseCase: SaveTrackLanguageUseCase

    beforeTest {
        saveTrackLanguageUseCase = mockk(relaxed = true)
        manager = AndroidPlayerManager(
            context = mockk(relaxed = true),
            saveTrackLanguageUseCase = saveTrackLanguageUseCase,
            decoderManagerHolder = mockk(relaxed = true)
        )
    }

    afterTest {
        unmockkStatic(Uri::class)
        unmockkStatic("org.jetbrains.compose.resources.StringResourcesKt")
    }

    fun positionAt(positionMs: Long) = Player.PositionInfo(
        null, 0, null, null, 0, positionMs, positionMs, -1, -1
    )

    fun setReady(player: Player = mockk(relaxed = true)) {
        val stateField = AndroidPlayerManager::class.java.getDeclaredField("_state").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        (stateField.get(manager) as MutableStateFlow<PlayerManager.State<Player>>).value = PlayerManager.State.Ready(player = player)
    }

    fun setReadySession(sessionId: String = "session", player: Player = mockk(relaxed = true)) {
        setReady(player)
        val sessionField = AndroidPlayerManager::class.java.getDeclaredField("currentSessionId").apply { isAccessible = true }
        sessionField.set(manager, sessionId)
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

    test("simultaneous plain and inline-styled unpositioned cues keep default bottom placement") {
        val plainCue = Cue.Builder().setText("First plain line").build()
        val styledSpanned = spannedText(
            text = "Second bold italic under color line",
            colorSpan(color = 0xFFFFFF00.toInt(), start = 25, end = 30),
            styleSpan(style = Typeface.BOLD, start = 7, end = 11),
            styleSpan(style = Typeface.ITALIC, start = 12, end = 18),
            underlineSpan(start = 19, end = 24),
        )
        val styledCue = Cue.Builder().setText(styledSpanned).build()

        manager.onCues(CueGroup(listOf(plainCue, styledCue), 0L))

        val emitted = manager.subtitles.first().shouldHaveSize(2)
        emitted[0].let {
            it.text shouldBe "First plain line"
            it.spans shouldBe emptyList()
            it.line shouldBe null
            it.position shouldBe null
            it.isPositioned shouldBe false
        }
        emitted[1].let {
            it.text shouldBe "Second bold italic under color line"
            it.line shouldBe null
            it.position shouldBe null
            it.isPositioned shouldBe false
            it.spans shouldContainExactly listOf(
                SubtitleTextSpan(start = 25, end = 30, color = 0xFFFFFF00.toInt()),
                SubtitleTextSpan(start = 7, end = 11, bold = true),
                SubtitleTextSpan(start = 12, end = 18, italic = true),
                SubtitleTextSpan(start = 19, end = 24, underline = true),
            )
        }
    }

    test("explicitly positioned inline-styled cue emits with authored coordinates and clamped spans") {
        val spanned = spannedText(
            text = "Top note",
            styleSpan(style = Typeface.ITALIC, start = -2, end = 20),
        )
        val positioned = Cue.Builder()
            .setText(spanned)
            .setLine(0.1f, Cue.LINE_TYPE_FRACTION)
            .setLineAnchor(Cue.ANCHOR_TYPE_START)
            .setPosition(0.8f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_END)
            .build()

        manager.onCues(CueGroup(listOf(positioned), 0L))

        val emitted = manager.subtitles.first().shouldHaveSize(1).first()
        emitted.isPositioned shouldBe true
        emitted.line shouldBe 0.1f
        emitted.lineAnchor shouldBe CueAnchor.START
        emitted.position shouldBe 0.8f
        emitted.positionAnchor shouldBe CueAnchor.END
        emitted.spans shouldContainExactly listOf(
            SubtitleTextSpan(start = 0, end = 8, italic = true)
        )
    }

    test("disconnect clears the last cues so they never leak into the next Media") {
        setReadySession(sessionId = "session")
        manager.onCues(CueGroup(listOf(Cue.Builder().setText("Last line of the previous file").build()), 0L))

        manager.disconnect(sessionId = "session")

        manager.subtitles.first() shouldBe emptyList()
    }

    test("bitmap cues reach the subtitles flow with normalized bounds and anchors") {
        val bitmapCue = Cue.Builder()
            .setBitmap(mockk<Bitmap>(relaxed = true))
            .setSize(0.6f)
            .setBitmapHeight(0.2f)
            .setPosition(0.15f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_START)
            .setLine(0.75f, Cue.LINE_TYPE_FRACTION)
            .setLineAnchor(Cue.ANCHOR_TYPE_START)
            .build()

        manager.onCues(CueGroup(listOf(bitmapCue), 0L))

        val emitted = manager.subtitles.first().shouldHaveSize(1).first()
        emitted.position shouldBe 0.15f
        emitted.positionAnchor shouldBe CueAnchor.START
        emitted.line shouldBe 0.75f
        emitted.lineAnchor shouldBe CueAnchor.START
        emitted.bitmap.shouldNotBeNull().let {
            it.widthFraction shouldBe 0.6f
            it.heightFraction shouldBe 0.2f
        }
    }

    test("consecutive distinct bitmap frames at identical bounds both emit on subtitles") {
        fun frameCue(bitmap: Bitmap) = Cue.Builder()
            .setBitmap(bitmap)
            .setSize(0.5f)
            .setBitmapHeight(0.15f)
            .setPosition(0.25f)
            .setPositionAnchor(Cue.ANCHOR_TYPE_START)
            .setLine(0.8f, Cue.LINE_TYPE_FRACTION)
            .setLineAnchor(Cue.ANCHOR_TYPE_START)
            .build()

        manager.subtitles.test {
            awaitItem() shouldBe emptyList()

            manager.onCues(CueGroup(listOf(frameCue(mockk(relaxed = true))), 0L))
            val frame1 = awaitItem().shouldHaveSize(1).first()

            manager.onCues(CueGroup(listOf(frameCue(mockk(relaxed = true))), 33_000L))
            val frame2 = awaitItem().shouldHaveSize(1).first()

            frame1 shouldNotBe frame2
            frame1.bitmap shouldNotBe frame2.bitmap
        }
    }

    test("bitmap cues with invalid dimensions are dropped") {
        val unsetCue = Cue.Builder()
            .setBitmap(mockk<Bitmap>(relaxed = true))
            .build()
        val outOfBoundsCue = Cue.Builder()
            .setBitmap(mockk<Bitmap>(relaxed = true))
            .setSize(1.2f)
            .setBitmapHeight(0.2f)
            .build()

        manager.onCues(CueGroup(listOf(unsetCue, outOfBoundsCue), 0L))

        manager.subtitles.first().map { it.bitmap } shouldBe listOf(null, null)
    }

    test("disconnect clears active bitmap cues immediately") {
        setReadySession(sessionId = "session")
        val bitmapCue = Cue.Builder()
            .setBitmap(mockk<Bitmap>(relaxed = true))
            .setSize(0.5f)
            .setBitmapHeight(0.2f)
            .build()
        manager.onCues(CueGroup(listOf(bitmapCue), 0L))
        manager.subtitles.first().first().bitmap.shouldNotBeNull()

        manager.disconnect(sessionId = "session")

        manager.subtitles.first() shouldBe emptyList()
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

    fun stubAndroidUriAndResources() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } answers {
            val raw = firstArg<String>()
            mockk<Uri>(relaxed = true) {
                every { this@mockk.toString() } returns raw
            }
        }
        mockkStatic("org.jetbrains.compose.resources.StringResourcesKt")
        coEvery { getString(Res.string.track) } returns "Track"
        coEvery { getString(Res.string.external_subtitle) } returns "External"
        coEvery { getString(Res.string.season_and_episode, any(), any()) } returns "S01E01"
    }

    fun subtitleFormat(
        id: String?,
        mimeType: String,
        language: String? = null,
        label: String? = null,
        codecs: String? = null,
        selectionFlags: Int = 0,
    ): Format = Format.Builder()
        .setId(id)
        .setSampleMimeType(mimeType)
        .setLanguage(language)
        .setLabel(label)
        .setCodecs(codecs)
        .setSelectionFlags(selectionFlags)
        .build()

    fun subtitleGroup(vararg formats: Format, selectedIndex: Int? = null): Tracks.Group = Tracks.Group(
        TrackGroup(*formats),
        false,
        IntArray(formats.size) { C.FORMAT_HANDLED },
        BooleanArray(formats.size) { it == selectedIndex },
    )

    fun setupSelectionPlayer(
        tracks: Tracks,
        initialParams: TrackSelectionParameters = TrackSelectionParameters.DEFAULT,
    ): Pair<Player, MutableList<TrackSelectionParameters>> {
        val capturedParams = mutableListOf<TrackSelectionParameters>()
        val player = mockk<Player>(relaxed = true) {
            every { trackSelectionParameters } answers { capturedParams.lastOrNull() ?: initialParams }
            every { trackSelectionParameters = capture(capturedParams) } just Runs
            every { currentTracks } returns tracks
        }
        setReady(player)
        manager.onTracksChanged(tracks)
        return player to capturedParams
    }

    test("playMedia attaches all external subtitles with extension MIME types, unique IDs, labels, and default flag only on first untagged") {
        stubAndroidUriAndResources()
        coEvery { saveTrackLanguageUseCase.getAudioLanguage() } returns "ja"
        coEvery { saveTrackLanguageUseCase.getSubtitlesLanguage() } returns "fr"
        coEvery { saveTrackLanguageUseCase.subtitlesEnabled() } returns true

        val mediaItemSlot = slot<MediaItem>()
        val player = mockk<Player>(relaxed = true) {
            every { trackSelectionParameters } returns TrackSelectionParameters.DEFAULT
            every { setMediaItem(capture(mediaItemSlot), any<Long>()) } just Runs
        }
        setReady(player)

        val movie = Movie(
            artworkId = 1L,
            title = "Test Movie",
            releaseDateString = "2025-01-01",
            description = "",
            voteAverage = 8f,
            voteCount = 10,
            duration = 120,
            currentTime = 5_000L,
            file = UserFile(
                name = "Episode01.mkv",
                path = "content://com.android.externalstorage.documents/tree/primary/document/100",
                source = FileSource.SAF,
            ),
            isAvailable = true,
        )
        val externalSubtitles = listOf(
            ExternalSubtitle(
                uri = "content://com.android.externalstorage.documents/tree/primary/document/101",
                fileName = "Episode01.fr.ass",
                extension = "ass",
                language = "fr",
            ),
            ExternalSubtitle(
                uri = "content://com.android.externalstorage.documents/tree/primary/document/102",
                fileName = "Episode01.fr.forced.ssa",
                extension = "ssa",
                language = "fr",
                tags = listOf("forced"),
            ),
            ExternalSubtitle(
                uri = "content://com.android.externalstorage.documents/tree/primary/document/103",
                fileName = "Episode01.ass",
                extension = "ass",
                language = null,
            ),
            ExternalSubtitle(
                uri = "content://com.android.externalstorage.documents/tree/primary/document/104",
                fileName = "Episode01.signs.srt",
                extension = "srt",
                language = null,
                tags = listOf("signs"),
            ),
            ExternalSubtitle(
                uri = "content://com.android.externalstorage.documents/tree/primary/document/105",
                fileName = "Episode01.en.vtt",
                extension = "vtt",
                language = "en",
            ),
        )

        manager.playMedia(media = movie, externalSubtitles = externalSubtitles)

        val configs = mediaItemSlot.captured.localConfiguration.shouldNotBeNull().subtitleConfigurations
        configs.shouldHaveSize(5)

        configs[0].mimeType shouldBe MimeTypes.TEXT_SSA
        configs[0].language shouldBe "fr"
        configs[0].label shouldBe "Français (ASS)"
        configs[0].selectionFlags shouldBe 0
        configs[0].id.shouldNotBeNull()

        configs[1].mimeType shouldBe MimeTypes.TEXT_SSA
        configs[1].language shouldBe "fr"
        configs[1].label shouldBe "Français · forced (SSA)"
        configs[1].selectionFlags shouldBe 0

        configs[2].mimeType shouldBe MimeTypes.TEXT_SSA
        configs[2].language.shouldBeNull()
        configs[2].label shouldBe "External (ASS)"
        configs[2].selectionFlags shouldBe C.SELECTION_FLAG_DEFAULT

        configs[3].mimeType shouldBe MimeTypes.APPLICATION_SUBRIP
        configs[3].language.shouldBeNull()
        configs[3].label shouldBe "External · signs (SRT)"
        configs[3].selectionFlags shouldBe 0

        configs[4].mimeType shouldBe MimeTypes.TEXT_VTT
        configs[4].language shouldBe "en"
        configs[4].label shouldBe "English (VTT)"
        configs[4].selectionFlags shouldBe 0

        configs.mapNotNull { it.id }.distinct().shouldHaveSize(5)
    }

    test("onTracksChanged orders external subtitles after embedded tracks using their external order") {
        val embeddedJaGroup = subtitleGroup(
            subtitleFormat(id = "1", mimeType = MimeTypes.TEXT_SSA, language = "ja", label = "Japanese Embedded")
        )
        val embeddedEnGroup = subtitleGroup(
            subtitleFormat(id = "2", mimeType = MimeTypes.APPLICATION_SUBRIP, language = "en", label = "English Embedded")
        )
        val extGroupSecond = subtitleGroup(
            subtitleFormat(id = "flux-ext-1", mimeType = MimeTypes.APPLICATION_SUBRIP, language = "fr", label = "Français (SRT)")
        )
        val extGroupFirst = subtitleGroup(
            subtitleFormat(id = "flux-ext-0", mimeType = MimeTypes.TEXT_SSA, language = "fr", label = "Français (ASS)")
        )
        setReady()

        manager.onTracksChanged(Tracks(listOf(embeddedJaGroup, embeddedEnGroup, extGroupSecond, extGroupFirst)))

        val ready = manager.flow.first() as PlayerManager.State.Ready
        ready.tracks.map { it.label } shouldContainExactly listOf(
            "Japanese Embedded",
            "English Embedded",
            "Français (ASS)",
            "Français (SRT)",
        )
    }

    test("automatic subtitle selection prefers external Track over embedded Track of the same preferred language") {
        val embeddedFrAss = subtitleGroup(
            subtitleFormat(id = "emb-1", mimeType = MimeTypes.TEXT_SSA, language = "fr", label = "French Embedded")
        )
        val externalFrSrt = subtitleGroup(
            subtitleFormat(
                id = "flux-ext-0",
                mimeType = MimeTypes.APPLICATION_MEDIA3_CUES,
                codecs = MimeTypes.APPLICATION_SUBRIP,
                language = "fr",
                label = "Français (SRT)",
            )
        )
        val (_, capturedParams) = setupSelectionPlayer(Tracks(listOf(embeddedFrAss, externalFrSrt)))

        manager.selectTrack(PlayerTrack(id = null, label = "Français", language = "fr", type = PlayerTrack.Type.SUBTITLES))

        val latestParams = capturedParams.last()
        latestParams.overrides.keys shouldContain externalFrSrt.mediaTrackGroup
        (manager.flow.first() as PlayerManager.State.Ready).selectedSubtitles?.label shouldBe "Français (SRT)"
    }

    test("automatic subtitle selection prefers external ASS/SSA over external SRT/VTT at equal preferred language") {
        val externalFrSrt = subtitleGroup(
            subtitleFormat(
                id = "flux-ext-1",
                mimeType = MimeTypes.APPLICATION_MEDIA3_CUES,
                codecs = MimeTypes.APPLICATION_SUBRIP,
                language = "fr",
                label = "Français (SRT)",
            )
        )
        val externalFrAss = subtitleGroup(
            subtitleFormat(
                id = "flux-ext-0",
                mimeType = MimeTypes.TEXT_SSA,
                language = "fr",
                label = "Français (ASS)",
            )
        )
        val (_, capturedParams) = setupSelectionPlayer(Tracks(listOf(externalFrSrt, externalFrAss)))

        manager.selectTrack(PlayerTrack(id = null, label = "Français", language = "fr", type = PlayerTrack.Type.SUBTITLES))

        val latestParams = capturedParams.last()
        latestParams.overrides.keys shouldContain externalFrAss.mediaTrackGroup
        (manager.flow.first() as PlayerManager.State.Ready).selectedSubtitles?.label shouldBe "Français (ASS)"
    }

    test("automatic subtitle selection matches regional external subtitle against primary preferred language") {
        val embeddedPtSrt = subtitleGroup(
            subtitleFormat(id = "emb-1", mimeType = MimeTypes.APPLICATION_SUBRIP, language = "pt", label = "Portuguese")
        )
        val externalPtBrAss = subtitleGroup(
            subtitleFormat(id = "flux-ext-0", mimeType = MimeTypes.TEXT_SSA, language = "pt-br", label = "Português (Brasil) (ASS)")
        )
        val (_, capturedParams) = setupSelectionPlayer(Tracks(listOf(embeddedPtSrt, externalPtBrAss)))

        manager.selectTrack(PlayerTrack(id = null, label = "Portuguese", language = "pt", type = PlayerTrack.Type.SUBTITLES))

        capturedParams.last().overrides.keys shouldContain externalPtBrAss.mediaTrackGroup
    }

    test("automatic subtitle selection prefers exact regional match over primary-only match among external tracks") {
        val externalPtPtAss = subtitleGroup(
            subtitleFormat(id = "flux-ext-0", mimeType = MimeTypes.TEXT_SSA, language = "pt-pt", label = "Português (Portugal) (ASS)")
        )
        val externalPtBrSrt = subtitleGroup(
            subtitleFormat(
                id = "flux-ext-1",
                mimeType = MimeTypes.APPLICATION_MEDIA3_CUES,
                codecs = MimeTypes.APPLICATION_SUBRIP,
                language = "pt-br",
                label = "Português (Brasil) (SRT)",
            )
        )
        val (_, capturedParams) = setupSelectionPlayer(Tracks(listOf(externalPtPtAss, externalPtBrSrt)))

        manager.selectTrack(PlayerTrack(id = null, label = "Português (Brasil)", language = "pt-BR", type = PlayerTrack.Type.SUBTITLES))

        capturedParams.last().overrides.keys shouldContain externalPtBrSrt.mediaTrackGroup
        (manager.flow.first() as PlayerManager.State.Ready).selectedSubtitles?.label shouldBe "Português (Brasil) (SRT)"
    }

    test("automatic subtitle selection falls back to first untagged external subtitle when no Track matches preferred language") {
        val embeddedJa = subtitleGroup(
            subtitleFormat(
                id = "emb-1",
                mimeType = MimeTypes.TEXT_SSA,
                language = "ja",
                label = "Japanese",
                selectionFlags = C.SELECTION_FLAG_DEFAULT,
            )
        )
        val externalUntaggedAss = subtitleGroup(
            subtitleFormat(
                id = "flux-ext-0",
                mimeType = MimeTypes.TEXT_SSA,
                language = null,
                label = "External (ASS)",
                selectionFlags = C.SELECTION_FLAG_DEFAULT,
            )
        )
        val externalUntaggedSrt = subtitleGroup(
            subtitleFormat(
                id = "flux-ext-1",
                mimeType = MimeTypes.APPLICATION_MEDIA3_CUES,
                codecs = MimeTypes.APPLICATION_SUBRIP,
                language = null,
                label = "External · alt (SRT)",
            )
        )
        val (_, capturedParams) = setupSelectionPlayer(
            Tracks(listOf(embeddedJa, externalUntaggedAss, externalUntaggedSrt))
        )

        manager.selectTrack(PlayerTrack(id = null, label = "Français", language = "fr", type = PlayerTrack.Type.SUBTITLES))

        val latestParams = capturedParams.last()
        latestParams.overrides.keys shouldContain externalUntaggedAss.mediaTrackGroup
        (manager.flow.first() as PlayerManager.State.Ready).selectedSubtitles?.label shouldBe "External (ASS)"
    }

    test("automatic subtitle selection does not fall back to untagged external when an embedded Track matches preferred language") {
        val embeddedFr = subtitleGroup(
            subtitleFormat(id = "emb-1", mimeType = MimeTypes.TEXT_SSA, language = "fr", label = "French Embedded")
        )
        val externalUntagged = subtitleGroup(
            subtitleFormat(
                id = "flux-ext-0",
                mimeType = MimeTypes.TEXT_SSA,
                language = null,
                label = "External (ASS)",
                selectionFlags = C.SELECTION_FLAG_DEFAULT,
            )
        )
        val (_, capturedParams) = setupSelectionPlayer(Tracks(listOf(embeddedFr, externalUntagged)))

        manager.selectTrack(PlayerTrack(id = null, label = "Français", language = "fr", type = PlayerTrack.Type.SUBTITLES))

        val latestParams = capturedParams.last()
        latestParams.overrides.keys shouldContain embeddedFr.mediaTrackGroup
        (manager.flow.first() as PlayerManager.State.Ready).selectedSubtitles?.label shouldBe "French Embedded"
    }

    test("disabling subtitles turns off text tracks and clears overrides even when external subtitles exist") {
        val externalFrAss = subtitleGroup(
            subtitleFormat(id = "flux-ext-0", mimeType = MimeTypes.TEXT_SSA, language = "fr", label = "Français (ASS)")
        )
        val (_, capturedParams) = setupSelectionPlayer(Tracks(listOf(externalFrAss)))

        manager.selectTrack(PlayerTrack(id = null, label = "Français", language = "fr", type = PlayerTrack.Type.SUBTITLES))
        manager.selectTrack(PlayerTrack.NO_SUBTITLES)

        val latestParams = capturedParams.last()
        latestParams.disabledTrackTypes shouldContain C.TRACK_TYPE_TEXT
        latestParams.overrides.isEmpty() shouldBe true
        (manager.flow.first() as PlayerManager.State.Ready).selectedSubtitles shouldBe PlayerTrack.NO_SUBTITLES
    }

    test("manual selection in Settings sheet overrides external priority") {
        val embeddedFrSrt = subtitleGroup(
            subtitleFormat(id = "emb-1", mimeType = MimeTypes.APPLICATION_SUBRIP, language = "fr", label = "French Embedded")
        )
        val externalFrAss = subtitleGroup(
            subtitleFormat(id = "flux-ext-0", mimeType = MimeTypes.TEXT_SSA, language = "fr", label = "Français (ASS)")
        )
        val (_, capturedParams) = setupSelectionPlayer(Tracks(listOf(embeddedFrSrt, externalFrAss)))

        val embeddedTrack = (manager.flow.first() as PlayerManager.State.Ready)
            .tracks
            .first { it.label == "French Embedded" }

        manager.selectTrack(embeddedTrack)

        val latestParams = capturedParams.last()
        latestParams.disabledTrackTypes shouldNotContain C.TRACK_TYPE_TEXT
        latestParams.overrides.keys shouldContain embeddedFrSrt.mediaTrackGroup
        (manager.flow.first() as PlayerManager.State.Ready).selectedSubtitles shouldBe embeddedTrack
    }
})
