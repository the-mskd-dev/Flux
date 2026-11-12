package com.mskd.flux.navigation.domain

import com.mskd.flux.core.model.artwork.ContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/** The page opened for an Artwork from the catalog, the search, the people page and the Private Folder. */
class ArtworkPageTest : FunSpec({

    test("a film opens its movie page, with its colour") {
        artworkPage(artworkId = 42L, type = ContentType.MOVIE, rgb = 7) shouldBe Route.Movie(artworkId = 42L, rgb = 7)
    }

    test("a series opens its Show page, with its colour") {
        artworkPage(artworkId = 42L, type = ContentType.SHOW, rgb = 7) shouldBe Route.Show(artworkId = 42L, rgb = 7)
    }

})
