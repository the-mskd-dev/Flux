package com.mskd.flux.utils

enum class Language(val code: String) {
    ENGLISH("en"),
    FRENCH("fr"),
    GERMAN("de"),
    ITALIAN("it"),
    SPANISH("es"),
    PORTUGUESE("pt"),
    DUTCH("nl"),
    KOREAN("ko"),
    JAPANESE("ja");


    val tmdbFormat: String get() = this.code.toTmdbFormat()

}