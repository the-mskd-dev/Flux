package com.kaem.flux.utils.extensions

import com.kaem.flux.model.artwork.Episode

fun List<Episode>.getPreviousEpisodesFor(episode: Episode) : List<Episode> {
    return this.filter {
        it.season < episode.season || (it.season == episode.season && it.number < episode.number)
    }
}

val List<Episode>.lastEpisode get() = this.maxWith(compareBy<Episode> { it.season }.thenBy { it.number })