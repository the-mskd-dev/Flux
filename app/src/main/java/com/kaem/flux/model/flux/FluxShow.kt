package com.kaem.flux.model.flux

data class FluxShow(
    val id: Int,
    val imagePath: String,
    val bannerPath: String,
    val episodes: List<FluxEpisode>
)