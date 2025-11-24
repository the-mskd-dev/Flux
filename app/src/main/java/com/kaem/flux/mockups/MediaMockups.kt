package com.kaem.flux.mockups

import com.kaem.flux.model.FileSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import com.kaem.flux.model.media.Status

object MediaMockups {

    val movieOverview get() = MediaOverview(
        id = 372058L,
        title = "Your name",
        type = ContentType.MOVIE,
        imagePath = "/vfJFJPepRKapMd5G2ro7klIRysq.jpg",
        bannerPath = "/8x9iKH8kWA0zdkgNdpAew7OstYe.jpg"
    )

    val movie get() = Movie(
        mediaId = 372058L,
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
            source = FileSource.LOCAL
        )
    )

    val showOverview get() = MediaOverview(
        id = 31910L,
        title = "Naruto Shippūden",
        type = ContentType.SHOW,
        imagePath = "/z0YhJvomqedHF85bplUJEotkN5l.jpg",
        bannerPath = "/71mASgFgSiPl9QUexVH8BubU0lD.jpg"
    )

    val episode1 get() = Episode(
        id = 761472L,
        number = 1,
        season = 1,
        imagePath = "/lFg0YnHI7sJkPSv38a8ctE96sqr.jpg",
        mediaId = 31910L,
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
        )
    )

    val episode2 get() = Episode(
        id = 761474L,
        number = 2,
        season = 1,
        imagePath = "/zbvJ4ts4JJmqP6koMNnLzBX6qiJ.jpg",
        mediaId = 31910L,
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
        )
    )

    val episode3 get() = Episode(
        id = 761474L,
        number =33,
        season = 2,
        imagePath = "/97O9irZPuV08ZLPQIPea434UG6R.jpg",
        mediaId = 31910L,
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
        )
    )

    val overviews get() = listOf(
        movieOverview,
        showOverview
    )

    val movies get() = listOf(
        movie
    )

    val episodes get() = listOf(
        episode1,
        episode2,
        episode3
    )

}