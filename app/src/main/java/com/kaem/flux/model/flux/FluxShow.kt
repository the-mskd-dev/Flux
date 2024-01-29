package com.kaem.flux.model.flux

import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

class FluxShow(
    id: Int,
    title: String,
    imagePath: String,
    bannerPath: String,
    releaseDateString: String
) : FluxArtworkSummary(
    id = id,
    title = title,
    imagePath = imagePath,
    bannerPath = bannerPath,
    releaseDateString = releaseDateString
) {

    constructor(tmdbArtwork: TMDBArtwork) : this(
        id = tmdbArtwork.id,
        title = tmdbArtwork.title,
        imagePath = tmdbArtwork.imagePath,
        bannerPath = tmdbArtwork.bannerPath,
        releaseDateString = tmdbArtwork.releaseDateString
    )

}