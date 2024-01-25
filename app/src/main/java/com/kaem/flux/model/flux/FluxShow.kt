package com.kaem.flux.model.flux

data class FluxShow(
    override val id: Int,
    override val title: String,
    override val imagePath: String,
    override val bannerPath: String,
    override val releaseDateString: String,
    val episodes: List<FluxEpisode>
) : FluxArtworkSummary