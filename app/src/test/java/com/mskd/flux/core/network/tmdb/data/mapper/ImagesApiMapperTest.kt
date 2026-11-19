package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.network.tmdb.data.dto.images.ImageDto
import com.mskd.flux.core.network.tmdb.data.dto.images.ImagesDto
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ImagesApiMapperTest : FunSpec({

    test("only PNG logos reach the Gallery, next to backdrops and posters") {

        val dto = ImagesDto(
            backdrops = listOf(ImageDto(filePath = "/b.jpg", aspectRatio = 1.78)),
            posters = listOf(ImageDto(filePath = "/p.jpg", aspectRatio = 0.67)),
            logos = listOf(
                ImageDto(filePath = "/l.png", aspectRatio = 2.5, voteAverage = 5.0),
                ImageDto(filePath = "/u.PNG", aspectRatio = 2.0),
                ImageDto(filePath = "/l.svg", aspectRatio = 2.5),
            ),
        )

        val images = dto.toDomain()

        images.map { it.path } shouldBe listOf("/b.jpg", "/p.jpg", "/l.png", "/u.PNG")
        images.map { it.type } shouldBe listOf(
            GalleryImageType.BACKDROP, GalleryImageType.POSTER, GalleryImageType.LOGO, GalleryImageType.LOGO,
        )

    }

    test("an image TMDB lists twice reaches the Gallery once, its path and type being its identity") {

        // Seen on TMDB (show 88046): the same poster twice in a row. A Gallery image is keyed by type + path
        val duplicated = ImageDto(filePath = "/dup.jpg", aspectRatio = 0.67, voteAverage = 0.166, languageCode = "en")
        val dto = ImagesDto(
            backdrops = listOf(ImageDto(filePath = "/dup.jpg", aspectRatio = 1.78)),
            posters = listOf(ImageDto(filePath = "/p.jpg", aspectRatio = 0.67), duplicated, duplicated),
        )

        val images = dto.toDomain()

        // Same path under another type is another image
        images.map { it.type to it.path } shouldBe listOf(
            GalleryImageType.BACKDROP to "/dup.jpg",
            GalleryImageType.POSTER to "/p.jpg",
            GalleryImageType.POSTER to "/dup.jpg",
        )

    }

    test("the Image language is kept on each Gallery image") {

        val dto = ImagesDto(
            backdrops = listOf(ImageDto(filePath = "/b.jpg", aspectRatio = 1.78)),
            posters = listOf(ImageDto(filePath = "/p.jpg", aspectRatio = 0.67, languageCode = "ja")),
            logos = listOf(ImageDto(filePath = "/l.png", aspectRatio = 2.5, languageCode = "fr")),
        )

        dto.toDomain().map { it.languageCode } shouldBe listOf(null, "ja", "fr")

    }

    test("logoPath picks the best Logo of the language, then English, then without language") {

        val dto = ImagesDto(
            logos = listOf(
                ImageDto(filePath = "/en.png", aspectRatio = 2.0, voteAverage = 9.0, languageCode = "en"),
                ImageDto(filePath = "/fr1.png", aspectRatio = 2.0, voteAverage = 1.0, languageCode = "fr"),
                ImageDto(filePath = "/fr2.png", aspectRatio = 2.0, voteAverage = 3.0, languageCode = "fr"),
            ),
        )

        dto.logoPath("fr-FR") shouldBe "/fr2.png"
        dto.logoPath("de-DE") shouldBe "/en.png"

    }

})
