package com.mskd.flux.core.model.artwork

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ArtworkHeaderModeTest : FunSpec({

    context("blurred header enabled") {

        test("a Movie page shows the blurred header") {
            ArtworkHeaderMode.of(pageType = ArtworkPageType.MOVIE, blurredHeader = true) shouldBe ArtworkHeaderMode.BLURRED
        }

        test("a Show page shows the blurred header") {
            ArtworkHeaderMode.of(pageType = ArtworkPageType.SHOW, blurredHeader = true) shouldBe ArtworkHeaderMode.BLURRED
        }

        test("a Season page shows the backdrop") {
            ArtworkHeaderMode.of(pageType = ArtworkPageType.SEASON, blurredHeader = true) shouldBe ArtworkHeaderMode.NORMAL
        }

    }

    context("blurred header disabled") {

        test("a Movie page shows the backdrop") {
            ArtworkHeaderMode.of(pageType = ArtworkPageType.MOVIE, blurredHeader = false) shouldBe ArtworkHeaderMode.NORMAL
        }

        test("a Show page shows the backdrop") {
            ArtworkHeaderMode.of(pageType = ArtworkPageType.SHOW, blurredHeader = false) shouldBe ArtworkHeaderMode.NORMAL
        }

        test("a Season page shows the backdrop") {
            ArtworkHeaderMode.of(pageType = ArtworkPageType.SEASON, blurredHeader = false) shouldBe ArtworkHeaderMode.NORMAL
        }

    }

})
