package com.kaem.flux.mockups

import com.kaem.flux.model.FileSource
import com.kaem.flux.model.UserFile

object FilesMockups {

    val localFiles = listOf(
        UserFile(
            name = "naruto_S01E01.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "naruto _S02E01.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = " naruto_S02E02.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "Naruto_S04E10.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "your name.mp4",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "spider-man(2002).mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        )
    )
}