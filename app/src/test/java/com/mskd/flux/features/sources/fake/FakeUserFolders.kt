package com.mskd.flux.features.sources.fake

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.domain.model.UserFolder

object FakeUserFolders {

    val folder1 = UserFolder(
        path = "path/to/folder",
        source = FileSource.SAF,
        isAvailable = true
    )

    val folder1sub1 = UserFolder(
        path = "path/to/folder/sub",
        source = FileSource.SAF,
        isAvailable = true
    )

    val folder1sub2 = UserFolder(
        path = "path/to/folder/sub",
        source = FileSource.SAF,
        isAvailable = true
    )

    val folder2 = UserFolder(
        path = "path/to/folder2",
        source = FileSource.SAF,
        isAvailable = true
    )

    val folderMissing = UserFolder(
        path = "path/to/missingFolder",
        source = FileSource.SAF,
        isAvailable = false
    )

}