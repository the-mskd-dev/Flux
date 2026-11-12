package com.mskd.flux.navigation.domain

import com.mskd.flux.core.model.artwork.ContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class IdentifiedDestinationTest : FunSpec({

    test("a movie leads to the movie page of the new Artwork") {
        identifiedDestination(artworkId = 42L, type = ContentType.MOVIE) shouldBe Route.Movie(artworkId = 42L, rgb = null)
    }

    test("a series leads to the Show page of the new Artwork") {
        identifiedDestination(artworkId = 42L, type = ContentType.SHOW) shouldBe Route.Show(artworkId = 42L, rgb = null)
    }

})
