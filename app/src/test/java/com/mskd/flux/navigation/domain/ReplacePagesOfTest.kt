package com.mskd.flux.navigation.domain

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ReplacePagesOfTest : FunSpec({

    val destination = Route.Show(artworkId = 42L, rgb = null)

    test("a Season page and the Show page below it are both replaced by the destination") {
        val stack = mutableListOf<NavKey>(Route.Catalog, Route.Show(artworkId = 1L, rgb = 3), Route.Season(artworkId = 1L, season = 2, rgb = 3))
        stack.replacePagesOf(artworkId = 1L, route = destination)
        stack shouldBe listOf(Route.Catalog, destination)
    }

    test("the pages of another Artwork stay in the stack") {
        val stack = mutableListOf<NavKey>(Route.Catalog, Route.Show(artworkId = 7L, rgb = null), Route.Season(artworkId = 1L, season = 2, rgb = null))
        stack.replacePagesOf(artworkId = 1L, route = destination)
        stack shouldBe listOf(Route.Catalog, Route.Show(artworkId = 7L, rgb = null), destination)
    }

    test("a page on its own is replaced like any other") {
        val stack = mutableListOf<NavKey>(Route.Catalog, Route.Show(artworkId = 1L, rgb = null))
        stack.replacePagesOf(artworkId = 1L, route = destination)
        stack shouldBe listOf(Route.Catalog, destination)
    }

})
