package com.mskd.flux.utils

import com.mskd.flux.core.model.player.PlayerTrack

expect fun systemLanguage(): String

expect fun languageDisplayName(tag: String): String

/** "fr-FR" -> "fr", "zh-Hant-TW" -> "zh" */
fun String.languageCode(): String = substringBefore('-')

/** "fr-FR" -> "FR", "zh-Hant-TW" -> "TW", "fr" -> null */
fun String.regionCode(): String? = split('-')
    .drop(1)
    .firstOrNull { it.length == 2 && it.all(Char::isLetter) }
    ?.uppercase()

fun String.toPlayerTrack(type: PlayerTrack.Type) = PlayerTrack(
    id = null,
    label = languageDisplayName(this),
    language = languageCode(),
    type = type
)

fun String.toTmdbFormat(): String =
    regionCode()?.let { "${languageCode()}-$it" } ?: languageCode()