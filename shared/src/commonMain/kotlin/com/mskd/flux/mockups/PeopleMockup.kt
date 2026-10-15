package com.mskd.flux.mockups

import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType

object PeopleMockup {

    val cast = listOf(
        Person(
            id = 82055,
            creditId = "525970a0760ee34661a30968",
            artworkId = 46260,
            name = "Junko Takeuchi",
            profilePath = "/zNjrblq3xS1idpCsmSl5P5eTon7.jpg",
            role = "Naruto Uzumaki (voice)",
            type = PersonType.CAST,
            personOrder = 0
        ),
        Person(
            id = 82056,
            creditId = "5259709f760ee34661a305d8",
            artworkId = 46260,
            name = "Noriaki Sugiyama",
            profilePath = "/szqqQ8T0gzuSxjU2rnWcthsaSJT.jpg",
            role = "Sasuke Uchiha (voice)",
            type = PersonType.CAST,
            personOrder = 1
        ),
        Person(
            id = 82057,
            creditId = "525970a0760ee34661a30a00",
            artworkId = 46260,
            name = "Chie Nakamura",
            profilePath = "/evB6vKUbm0CKgBcgikPeFXdwd29.jpg",
            role = "Sakura Haruno (voice)",
            type = PersonType.CAST,
            personOrder = 2
        ),
    )

    val crew = listOf(
        Person(
            id = 89963,
            creditId = "5259709f760ee34661a305d8",
            artworkId = 46260,
            name = "Masashi Kishimoto",
            profilePath = "/8hoQQiQEak3HxCDhEIDcAZ8QI41.jpg",
            role = "Author",
            type = PersonType.CREW,
            personOrder = 0
        ),
        Person(
            id = 551956,
            creditId = "63221492fd4a96007f6152e2",
            artworkId = 46260,
            name = "Seiji Morita",
            profilePath = null,
            role = "Editor",
            type = PersonType.CREW,
            personOrder = 1
        ),
        Person(
            id = 1123380,
            creditId = "632213cef14dad007d61abc7",
            artworkId = 46260,
            name = "Toshio Masuda",
            profilePath = null,
            role = "Music",
            type = PersonType.CREW,
            personOrder = 2
        ),
    )

    val all = cast + crew

}