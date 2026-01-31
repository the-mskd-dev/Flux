package com.kaem.flux.utils.extensions

import com.kaem.flux.model.artwork.Episode

fun List<Episode>.getPreviousEpisodesFor(episode: Episode) : List<Episode> {
    return this.filter {
        it.season < episode.season || (it.season == episode.season && it.number < episode.number)
    }
}

val List<Episode>.lastEpisode get() = this.maxWith(compareBy<Episode> { it.season }.thenBy { it.number })

fun List<Episode>.getPreviousEpisodeFor(episode: Episode) : Episode? {
    return this
        .filter { it.season == episode.season }
        .sortedBy { it.number }
        .let {

            val index = it.indexOf(episode)

            when {
                index < 0 || index == 0 -> null
                else -> it[index - 1]
            }
        }
}

fun List<Episode>.getNextEpisodeFor(episode: Episode) : Episode? {
    return this
        .filter { it.season == episode.season }
        .sortedBy { it.number }
        .let {

            val index = it.indexOf(episode)

            when {
                index < 0 || index == it.lastIndex -> null
                else -> it[index + 1]
            }
    }
}