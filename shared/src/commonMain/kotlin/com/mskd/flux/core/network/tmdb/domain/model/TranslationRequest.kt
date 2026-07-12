package com.mskd.flux.core.network.tmdb.domain.model

import java.util.Locale

sealed class TranslationRequest(val language: Locale) {
    class Movie(val artworkId: Long, language: Locale) : TranslationRequest(language)
    class Show(val artworkId: Long, language: Locale) : TranslationRequest(language)
    class Episode(val artworkId: Long, val season: Int, val number: Int, language: Locale) : TranslationRequest(language)
    class Season(val artworkId: Long, val season: Int, language: Locale) : TranslationRequest(language)
}