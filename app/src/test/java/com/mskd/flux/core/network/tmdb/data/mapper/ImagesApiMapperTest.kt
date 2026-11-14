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

})
