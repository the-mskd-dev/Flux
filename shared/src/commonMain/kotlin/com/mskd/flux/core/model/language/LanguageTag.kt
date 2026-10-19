package com.mskd.flux.core.model.language

/** "fr-FR" -> "fr", "zh-Hant-TW" -> "zh" */
fun String.languageCode(): String = substringBefore('-')


fun String.toTmdbFormat(): String = Language.fromCode(this).toString()