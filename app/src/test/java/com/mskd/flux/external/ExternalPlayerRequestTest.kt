package com.mskd.flux.external

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ExternalPlayerRequestTest : FunSpec({

    context("extras") {
        test("resumes at the saved Progress") {
            val extras = ExternalPlayerRequest.extras(title = "Movie", currentTime = 4200L)
            extras["position"] shouldBe 4200L
            extras["start_position"] shouldBe 4200L
            extras.containsKey("from_start") shouldBe false
            extras["return_result"] shouldBe true
        }

        test("starts from the beginning without Progress") {
            val extras = ExternalPlayerRequest.extras(title = "Movie", currentTime = 0L)
            extras["from_start"] shouldBe true
            extras["position"] shouldBe 1L
        }

        test("carries the title under every title key") {
            val extras = ExternalPlayerRequest.extras(title = "Movie", currentTime = 0L)
            listOf("title", "android.intent.extra.TITLE", "filename", "media_title").forEach { extras[it] shouldBe "Movie" }
        }
    }

    context("parsePosition") {
        test("long") { ExternalPlayerRequest.parsePosition { if (it == "position") 5000L else null } shouldBe 5000L }
        test("int") { ExternalPlayerRequest.parsePosition { if (it == "end_by") 3000 else null } shouldBe 3000L }
        test("other keys") { ExternalPlayerRequest.parsePosition { if (it == "playback_position") 7L else null } shouldBe 7L }
        test("missing") { ExternalPlayerRequest.parsePosition { null } shouldBe null }
        test("-1 is not a position") { ExternalPlayerRequest.parsePosition { if (it == "position") -1L else null } shouldBe null }
    }

})
