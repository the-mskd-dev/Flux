package com.mskd.flux.features.gallery.ui.viewer

import com.mskd.flux.configs.fluxExtensions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class ViewerControlsStateTest : FunSpec({

    val testDispatcher: TestDispatcher = fluxExtensions(StandardTestDispatcher())

    lateinit var scope: CoroutineScope
    lateinit var controls: ViewerControlsState

    fun advanceAndRun(millis: Long) {
        testDispatcher.scheduler.advanceTimeBy(millis)
        testDispatcher.scheduler.runCurrent()
    }

    beforeTest {
        scope = CoroutineScope(testDispatcher)
        controls = ViewerControlsState(scope)
    }

    afterTest {
        scope.cancel()
    }

    test("controls are hidden when the viewer opens") {
        controls.visible shouldBe false
    }

    test("a tap shows the controls, another tap hides them") {
        controls.onTap()
        controls.visible shouldBe true
        controls.onTap()
        controls.visible shouldBe false
    }

    test("shown controls hide by themselves after 3 s without interaction") {
        controls.onTap()
        advanceAndRun(2_999)
        controls.visible shouldBe true
        advanceAndRun(2)
        controls.visible shouldBe false
    }

    test("a button interaction restarts the 3 s delay") {
        controls.onTap()
        advanceAndRun(2_000)
        controls.onButtonInteraction()
        advanceAndRun(2_500)
        controls.visible shouldBe true
        advanceAndRun(501)
        controls.visible shouldBe false
    }

    test("controls stay visible while an image download runs, the delay restarts once it ends") {
        controls.onTap()
        controls.onButtonInteraction()
        controls.onDownloadRunningChange(true)
        advanceAndRun(10_000)
        controls.visible shouldBe true

        controls.onDownloadRunningChange(false)
        advanceAndRun(2_999)
        controls.visible shouldBe true
        advanceAndRun(2)
        controls.visible shouldBe false
    }

    test("hiding the controls by a tap cancels the delay, showing them again starts a new one") {
        controls.onTap()
        advanceAndRun(2_000)
        controls.onTap()
        controls.onTap()
        advanceAndRun(2_999)
        controls.visible shouldBe true
        advanceAndRun(2)
        controls.visible shouldBe false
    }

    test("hiding the controls cancels the delay, the next tap shows them again for 3 s") {
        controls.onTap()
        advanceAndRun(2_000)
        controls.hide()
        controls.visible shouldBe false
        controls.onTap()
        advanceAndRun(2_999)
        controls.visible shouldBe true
        advanceAndRun(2)
        controls.visible shouldBe false
    }

})
