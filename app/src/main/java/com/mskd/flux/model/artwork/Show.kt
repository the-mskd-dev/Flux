package com.mskd.flux.model.artwork

data class Show(
    val artwork: Artwork,
    val seasons: List<Season>,
    val episodes: List<Episode>
)
