package com.mskd.flux.utils

enum class Language(val code: String) {
    ENGLISH("en"),
    FRENCH("fr"),
    KOREAN("ko"),
    JAPANESE("ja"),
    GERMAN("de"),
    ITALIAN("it"),
    SPANISH("es"),
    PORTUGUESE("pt"),
    DUTCH("nl"),
    ARABIC("ar"),
    CHINESE("zh")
    ;


    val tmdbFormat: String get() = this.code.toTmdbFormat()

}