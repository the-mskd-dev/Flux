package com.mskd.flux.external

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import com.mskd.flux.features.player.domain.playback.ExternalLaunchResult
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk

class AndroidExternalPlayerAdapterTest : FunSpec({

    var stops = 0
    var starts = 0
    lateinit var adapter: AndroidExternalPlayerAdapter
    lateinit var launcher: ActivityResultLauncher<Intent>

    beforeTest {
        stops = 0
        starts = 0
        launcher = mockk(relaxed = true)
        adapter = AndroidExternalPlayerAdapter(mockk(relaxed = true)).apply {
            startService = { starts++ }
            stopService = { stops++ }
            createIntent = { mockk(relaxed = true) }
        }
    }

    val media = MediaMockups.movie

    test("a detached launcher yields Failed and starts nothing") {
        adapter.launch(media).shouldBeInstanceOf<ExternalLaunchResult.Failed>()
        starts shouldBe 0
    }

    test("a launcher detached by its own Activity yields Failed") {
        adapter.attach(launcher)
        adapter.detach(launcher)
        adapter.launch(media).shouldBeInstanceOf<ExternalLaunchResult.Failed>()
    }

    test("detach ignores a launcher that is not the attached one") {
        adapter.attach(launcher)
        adapter.detach(mockk(relaxed = true))
        adapter.launch(media) shouldBe ExternalLaunchResult.Launched
    }

    test("a successful launch starts the service and keeps it running") {
        adapter.attach(launcher)
        adapter.launch(media) shouldBe ExternalLaunchResult.Launched
        starts shouldBe 1
        stops shouldBe 0
    }

    test("launch failures stop the service") {
        adapter.attach(launcher)
        every { launcher.launch(any()) } throws android.content.ActivityNotFoundException()
        adapter.launch(media) shouldBe ExternalLaunchResult.NoPlayerInstalled
        every { launcher.launch(any()) } throws SecurityException()
        adapter.launch(media) shouldBe ExternalLaunchResult.PermissionDenied
        every { launcher.launch(any()) } throws IllegalStateException()
        adapter.launch(media).shouldBeInstanceOf<ExternalLaunchResult.Failed>()
        stops shouldBe 3
    }

    test("a result stops the service and returns the position") {
        val bundle = mockk<Bundle>()
        every { bundle.get(any<String>()) } answers { if (firstArg<String>() == "position") 4000L else null }
        val data = mockk<Intent>()
        every { data.extras } returns bundle
        adapter.onResult(data) shouldBe 4000L
        stops shouldBe 1
    }

    test("a cancelled result (no data) stops the service") {
        adapter.onResult(null) shouldBe null
        stops shouldBe 1
    }

    test("a result without position stops the service") {
        val data = mockk<Intent>()
        every { data.extras } returns Bundle()
        adapter.onResult(data) shouldBe null
        stops shouldBe 1
    }
})
