package com.mskd.flux.navigation.domain

import androidx.navigation3.runtime.NavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json

/** A back stack saved by a previous version is decoded with the same routes module as [navConfig]. */
class UnrestorableRouteTest : FunSpec({

    val json = Json {
        serializersModule = routesSerializersModule
        ignoreUnknownKeys = true
    }

    fun decode(payload: String): NavKey = json.decodeFromString(PolymorphicSerializer(NavKey::class), payload)

    test("the Artwork route of a previous version is not restored, and does not crash") {
        decode("""{"type":"com.mskd.flux.navigation.domain.Route.Artwork","artworkId":1,"season":2,"rgb":null}""") shouldBe Route.Unrestorable
    }

    test("the movie route is restored") {
        decode("""{"type":"com.mskd.flux.navigation.domain.Route.Movie","artworkId":1,"rgb":3}""") shouldBe Route.Movie(artworkId = 1L, rgb = 3)
    }

})
