package com.mskd.flux.features.sources.fake

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.sources.domain.model.UserFolder

object FakeUserFolders {

    val folder1 = UserFolder(
        path = "path/to/folder",
        source = FileSource.LOCAL,
        status = UserFolder.Status.AVAILABLE
    )

    val folder1sub1 = UserFolder(
        path = "path/to/folder/sub",
        source = FileSource.LOCAL,
        status = UserFolder.Status.AVAILABLE
    )

    val folder1sub2 = UserFolder(
        path = "path/to/folder/sub",
        source = FileSource.LOCAL,
        status = UserFolder.Status.AVAILABLE
    )

    val folder2 = UserFolder(
        path = "path/to/folder2",
        source = FileSource.LOCAL,
        status = UserFolder.Status.AVAILABLE
    )

    val folder3 = UserFolder(
        path = "path/to/missingFolder",
        source = FileSource.LOCAL,
        status = UserFolder.Status.MISSING
    )

}