package com.mskd.flux.utils.extensions

import com.mskd.flux.model.Status
import com.mskd.flux.model.artwork.Episode

fun List<Episode>.sort() : List<Episode> {
    return this.sortedWith(compareBy({ it.season }, { it.number }))
}
fun List<Episode>.getPreviousEpisodesFor(episode: Episode) : List<Episode> {
    return this.filter {
        it.season < episode.season || (it.season == episode.season && it.number < episode.number)
    }
}

fun List<Episode>.firstEpisodeToWatch() : Episode? {
    val sortedEpisodes = this.sort()

    return sortedEpisodes.firstOrNull { it.status == Status.IS_WATCHING } // First episode watching
        ?: sortedEpisodes.firstOrNull { it.status == Status.TO_WATCH } // First episode to watch
        ?: sortedEpisodes.firstOrNull() // First episode
}

val List<Episode>.firstEpisode get() = this.minWith(compareBy<Episode> { it.season }.thenBy { it.number })
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