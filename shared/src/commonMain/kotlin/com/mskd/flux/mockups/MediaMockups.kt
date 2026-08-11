package com.mskd.flux.mockups

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import kotlinx.collections.immutable.toImmutableList
import kotlin.time.Duration.Companion.minutes

object MediaMockups {

    val movieArtwork = Artwork(
        id = 372058L,
        title = "Your name",
        type = ContentType.MOVIE,
        genreIds = DetailsMockup.movieGenres.take(4).map { it.id }.toImmutableList(),
        imagePath = "/vfJFJPepRKapMd5G2ro7klIRysq.jpg",
        bannerPath = "/8x9iKH8kWA0zdkgNdpAew7OstYe.jpg"
    )

    val movieArtwork2 = Artwork(
        id = 969681L,
        title = "Spider-Man: Brand new day",
        type = ContentType.MOVIE,
        genreIds = DetailsMockup.movieGenres.takeLast(4).map { it.id }.toImmutableList(),
        imagePath = "/6Q21yptoOCUq4ErwVncesLPVplb.jpg",
        bannerPath = "/kbvNLChuMl2nyAzPZvqkD8hZGZn.jpg"
    )

    val movie = Movie(
        artworkId = 372058L,
        title = "Your name",
        releaseDateString = "2016-08-26",
        description = "High schoolers Mitsuha and Taki are complete strangers living separate lives. But one night, they suddenly switch places. Mitsuha wakes up in Taki’s body, and he in hers. This bizarre occurrence continues to happen randomly, and the two must adjust their lives around each other.",
        voteAverage = 8.5f,
        voteCount = 11518,
        duration = 106,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "your_name.mkv",
            addedDateTime = 0L,
            path = "path/your_name.mkv",
            source = FileSource.LOCAL,

        ),
        isAvailable = true
    )

    val movie2 = Movie(
        artworkId = 969681L,
        title = "Spider-Man: Brand New Day",
        releaseDateString = "2026-07-28",
        description = "Fighting crime full-time as Spider-Man in a world that doesn't remember him—and the pressure of seeing his old friends move on without him—sparks a change in Peter Parker he may not have the power to control. But that transformation might also be the only thing that can stop a shocking new threat to the city and those he loves - a powerful villain no one can even see.",
        voteAverage = 9.1f,
        voteCount = 616,
        duration = 145,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "spider-man_brand_new_day.mkv",
            addedDateTime = 0L,
            path = "path/spider-man_brand_new_day.mkv",
            source = FileSource.LOCAL,

            ),
        isAvailable = true
    )

    val showArtwork = Artwork(
        id = 31910L,
        title = "Naruto Shippūden",
        type = ContentType.SHOW,
        genreIds = DetailsMockup.showGenres.take(4).map { it.id }.toImmutableList(),
        imagePath = "/z0YhJvomqedHF85bplUJEotkN5l.jpg",
        bannerPath = "/71mASgFgSiPl9QUexVH8BubU0lD.jpg"
    )

    val episode1 = Episode(
        id = 761472L,
        number = 1,
        season = 1,
        imagePath = "/lFg0YnHI7sJkPSv38a8ctE96sqr.jpg",
        artworkId = 31910L,
        title = "Homecoming",
        releaseDateString = "2007-02-15",
        description = "A figure passes through the gates. It's an older Naruto, who has returned from a long training journey with Jiraiya. Naruto Uzumaki is back!",
        voteAverage = 7.8f,
        voteCount = 8,
        duration = 23,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "naruto_shippuuden_S01E01.mkv",
            addedDateTime = 0L,
            path = "path/naruto_shippuuden_S01E01.mkv",
            source = FileSource.LOCAL
        ),
        isAvailable = true
    )

    val episode2 = Episode(
        id = 761473L,
        number = 2,
        season = 1,
        imagePath = "/zbvJ4ts4JJmqP6koMNnLzBX6qiJ.jpg",
        artworkId = 31910L,
        title = "The Akatsuki Makes Its Move",
        releaseDateString = "2007-02-15",
        description = "Naruto and Sakura team up and challenge Kakashi to a survival challenge to show off their progress.",
        voteAverage = 6.8f,
        voteCount = 6,
        duration = 23,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "naruto_shippuuden_S01E02.mkv",
            addedDateTime = 0L,
            path = "path/naruto_shippuuden_S01E02.mkv",
            source = FileSource.LOCAL
        ),
        isAvailable = true
    )

    val episode3 = Episode(
        id = 761474L,
        number = 33,
        season = 2,
        imagePath = "/97O9irZPuV08ZLPQIPea434UG6R.jpg",
        artworkId = 31910L,
        title = "The New Target",
        releaseDateString = "2007-11-08",
        description = "Teams Kakashi and Guy return home to Konoha. As Kakashi rests in the infirmary, Sakura tells Tsunade about the information Sasori gave her as a reward for defeating him. Sasori had a meeting scheduled with a spy from within Orochimaru's ranks in 10 days. 4 days have passed since Sakura received this information, and now Naruto and Sakura need to recruit a new team member to go and investigate. As Naruto searches Konoha for a willing recruit, he meets some of his old friends. Just as Choji agrees to help, Shikamaru, Choji, and Naruto are attacked by a Konoha shinobi unknown to them, Sai.",
        voteAverage = 7.8f,
        voteCount = 6,
        duration = 23,
        currentTime = 0L,
        status = Status.TO_WATCH,
        file = UserFile(
            name = "naruto_shippuuden_S02E33.mkv",
            addedDateTime = 0L,
            path = "path/naruto_shippuuden_S02E33.mkv",
            source = FileSource.LOCAL
        ),
        isAvailable = true
    )

    val unknownArtwork = Artwork.UNKNOWN

    val unknownEpisode = Episode(
        file = UserFile(
            name = "unknown episode S02E03.mkv",
            addedDateTime = 0L,
            path = "path/unknown_episode_S02E03.mkv",
            source = FileSource.LOCAL
        ),
        duration = 23
    )

    val unknownMovie = Episode(
        file = UserFile(
            name = "unknown movie.mkv",
            addedDateTime = 0L,
            path = "path/unknown_movie.mkv",
            source = FileSource.LOCAL
        ),
        duration = 95
    )

    val artworks = listOf(
        movieArtwork,
        movieArtwork2,
        showArtwork,
        unknownArtwork
    )

    val movies = listOf(
        movie,
        movie2
    )

    val episodes = listOf(
        episode1,
        episode2,
        episode3
    )

    val episodesWithStatus = listOf(
        episode1,
        episode2.copy(status = Status.IS_WATCHING, currentTime = 10.minutes.inWholeMilliseconds),
        episode3.copy(status = Status.WATCHED),
    )

    val unknowns = listOf(
        unknownEpisode,
        unknownMovie
    )

    val season1 = Season(
        id = 1001L,
        artworkId = 31910L,
        title = "Season 1",
        description = "The first season of Naruto Shippūden.",
        imagePath = "/season1_image.jpg",
        season = 1
    )

    val season2 = Season(
        id = 1002L,
        artworkId = 31910L,
        title = "Season 2",
        description = "The second season of Naruto Shippūden.",
        imagePath = "/season2_image.jpg",
        season = 2
    )

    val season3 = Season(
        id = 1003L,
        artworkId = 31910L,
        title = "Season 3",
        description = "The third season of Naruto Shippūden.",
        imagePath = "/season3_image.jpg",
        season = 3
    )

    val seasons = listOf(
        season1,
        season2,
        season3
    )

    val allMedias = movies + episodes + unknowns

    val fullMovie = FullArtwork.FullMovie(
        artwork = movieArtwork,
        movie = movie,
        genres = DetailsMockup.movieGenres.take(3).toImmutableList()
    )

    val fullShow = FullArtwork.FullShow(
        artwork = showArtwork,
        seasons = seasons.toImmutableList(),
        episodes = episodes.toImmutableList(),
        genres = DetailsMockup.showGenres.take(3).toImmutableList()
    )

}