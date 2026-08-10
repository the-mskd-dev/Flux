package com.mskd.flux.mockups

import com.mskd.flux.core.model.artwork.Genre
import kotlinx.collections.immutable.persistentListOf

object DetailsMockup {

    val movieGenres = persistentListOf(
        Genre(
            id = 28,
            name = "Action"
        ),
        Genre(
            id = 12,
            name = "Abenteuer"
        ),
        Genre(
            id = 16,
            name = "Animation"
        ),
        Genre(
            id = 35,
            name = "Komödie"
        ),
        Genre(
            id = 80,
            name = "Krimi"
        ),
        Genre(
            id = 99,
            name = "Dokumentarfilm"
        ),
        Genre(
            id = 18,
            name = "Drama"
        ),
        Genre(
            id = 10751,
            name = "Familie"
        ),
        Genre(
            id = 14,
            name = "Fantasy"
        ),
        Genre(
            id = 36,
            name = "Historie"
        ),
        Genre(
            id = 27,
            name = "Horror"
        ),
        Genre(
            id = 10402,
            name = "Musik"
        ),
        Genre(
            id = 9648,
            name = "Mystery"
        ),
        Genre(
            id = 10749,
            name = "Liebesfilm"
        ),
        Genre(
            id = 878,
            name = "Science Fiction"
        ),
        Genre(
            id = 10770,
            name = "TV-Film"
        ),
        Genre(
            id = 53,
            name = "Thriller"
        ),
        Genre(
            id = 10752,
            name = "Kriegsfilm"
        ),
        Genre(
            id = 37,
            name = "Western"
        )
    )

    val showGenres = persistentListOf(

        Genre(
            id = 10759,
            name = "Action & Adventure"
        ),
        Genre(
            id = 16,
            name = "Animation"
        ),
        Genre(
            id = 35,
            name = "Komödie"
        ),
        Genre(
            id = 80,
            name = "Krimi"
        ),
        Genre(
            id = 99,
            name = "Dokumentarfilm"
        ),
        Genre(
            id = 18,
            name = "Drama"
        ),
        Genre(
            id = 10751,
            name = "Familie"
        ),
        Genre(
            id = 10762,
            name = "Kids"
        ),
        Genre(
            id = 9648,
            name = "Mystery"
        ),
        Genre(
            id = 10763,
            name = "News"
        ),
        Genre(
            id = 10764,
            name = "Reality"
        ),
        Genre(
            id = 10765,
            name = "Sci-Fi & Fantasy"
        ),
        Genre(
            id = 10766,
            name = "Soap"
        ),
        Genre(
            id = 10767,
            name = "Talk"
        ),
        Genre(
            id = 10768,
            name = "War & Politics"
        ),
        Genre(
            id = 37,
            name = "Western"
        ),
    )

    val allGenres = (movieGenres + showGenres).distinctBy { it.id }

}