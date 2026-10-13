package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.PeopleType
import com.mskd.flux.core.network.tmdb.data.dto.cast.CastMemberDto
import com.mskd.flux.core.network.tmdb.data.dto.cast.CreditsDto
import com.mskd.flux.core.network.tmdb.data.dto.cast.CrewMemberDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class CreditsApiMapperTest : FunSpec({

    val artworkId = 550L

    val dto = CreditsDto(
        id = artworkId,
        cast = listOf(
            CastMemberDto(
                id = 819,
                name = "Edward Norton",
                profilePath = "/8nytsqL59SFJTVYVrN72k6qkGgJ.jpg",
                creditId = "52fe4250c3a36847f80149f3",
                character = "The Narrator",
                order = 0
            )
        ),
        crew = listOf(
            CrewMemberDto(
                id = 376,
                name = "Arnon Milchan",
                profilePath = "/b2hBExX4NnczNAnLuTBF4kmNhZm.jpg",
                creditId = "55731b8192514111610027d7",
                job = "Executive Producer",
                department = "Production"
            )
        )
    )

    test("maps cast and crew into a single unified People list") {
        // When
        val result = dto.toDomain(artworkId = artworkId)

        // Then
        result shouldHaveSize 2
    }

    test("maps a cast member with its character as role and CAST type") {
        // When
        val result = dto.toDomain(artworkId = artworkId)

        // Then
        val cast = result.first { it.type == PeopleType.CAST }
        cast.id shouldBe 819
        cast.creditId shouldBe "52fe4250c3a36847f80149f3"
        cast.artworkId shouldBe artworkId
        cast.name shouldBe "Edward Norton"
        cast.profilePath shouldBe "/8nytsqL59SFJTVYVrN72k6qkGgJ.jpg"
        cast.role shouldBe "The Narrator"
        cast.personOrder shouldBe 0
    }

    test("maps a crew member with its job as role and CREW type") {
        // When
        val result = dto.toDomain(artworkId = artworkId)

        // Then
        val crew = result.first { it.type == PeopleType.CREW }
        crew.id shouldBe 376
        crew.creditId shouldBe "55731b8192514111610027d7"
        crew.artworkId shouldBe artworkId
        crew.name shouldBe "Arnon Milchan"
        crew.role shouldBe "Executive Producer"
        crew.personOrder shouldBe 0
    }

})