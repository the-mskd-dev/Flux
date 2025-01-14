package com.kaem.flux.mockups

import com.kaem.flux.model.FileSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status

object ArtworkMockups {

    val movieOverview get() = ArtworkOverview(
        id = 1L,
        title = "Your name",
        type = ContentType.MOVIE
    )

    val movie get() = Movie(
        artworkId = 1L,
        title = "Your name",
        releaseDateString = "2016-12-28",
        description = "description",
        voteAverage = 10f,
        voteCount = 2809,
        duration = 110,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "your_name.mkv",
            addedDateTime = 0L,
            path = "path",
            source = FileSource.LOCAL
        )
    )

    val showOverview get() = ArtworkOverview(
        id = 2L,
        title = "Naruto",
        type = ContentType.SHOW
    )

    val episode1 get() = Episode(
        id = 3L,
        number = 1,
        season = 1,
        imagePath = "",
        artworkId = 2L,
        title = "Naruto episode 1",
        releaseDateString = "2016-12-28",
        description = "description",
        voteAverage = 10f,
        voteCount = 2809,
        duration = 22,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "naruto_S01E01.mkv",
            addedDateTime = 0L,
            path = "path",
            source = FileSource.LOCAL
        )
    )

    val episode2 get() = Episode(
        id = 4L,
        number = 2,
        season = 1,
        imagePath = "",
        artworkId = 2L,
        title = "Naruto episode 2",
        releaseDateString = "2016-12-28",
        description = "description",
        voteAverage = 10f,
        voteCount = 289,
        duration = 23,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "naruto_S01E02.mkv",
            addedDateTime = 0L,
            path = "path",
            source = FileSource.LOCAL
        )
    )

    val overviews get() = listOf(
        movieOverview,
        showOverview
    )

    val episodes get() = listOf(
        episode1,
        episode2
    )

}