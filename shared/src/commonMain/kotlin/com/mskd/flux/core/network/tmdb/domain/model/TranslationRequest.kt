package com.mskd.flux.core.network.tmdb.domain.model

sealed class TranslationRequest(val language: String) {
    class Movie(val artworkId: Long, language: String) : TranslationRequest(language)
    class Show(val artworkId: Long, language: String) : TranslationRequest(language)
    class Episode(val artworkId: Long, val season: Int, val number: Int, language: String) : TranslationRequest(language)
    class Season(val artworkId: Long, val season: Int, language: String) : TranslationRequest(language)
}