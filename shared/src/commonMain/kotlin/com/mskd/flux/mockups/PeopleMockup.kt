package com.mskd.flux.mockups

import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.features.people.domain.model.PersonDetails
import kotlinx.collections.immutable.toPersistentList

object PeopleMockup {

    val cast = listOf(
        Person(
            id = 82055,
            creditId = "525970a0760ee34661a30968",
            artworkId = 46260,
            name = "Junko Takeuchi",
            originalName = "竹内順子",
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
            originalName = "杉山紀彰",
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
            originalName = "中村千絵",
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
            originalName = "岸本斉史",
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
            originalName = "森田清次",
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
            originalName = "増田俊郎",
            profilePath = null,
            role = "Music",
            type = PersonType.CREW,
            personOrder = 2
        ),
    )

    val all = cast + crew

    fun toPersonDetails(person: Person = cast.first()) = PersonDetails(
        id = person.id,
        name = person.name,
        originalName = person.originalName,
        profilePath = person.profilePath,
        birthday = "01-01-2001",
        deathday = null,
        placeOfBirth = "city",
        biography = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        knownForDepartment = "Acting",
        alsoKnownAs = listOf("Name 1", "Name 2", "Name 3"),
        homepage = null,
        imdbId = null,
        artworks = MediaMockups.artworks.toPersistentList()
    )

}