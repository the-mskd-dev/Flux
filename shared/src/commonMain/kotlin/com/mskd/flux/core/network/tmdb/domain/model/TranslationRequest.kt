package com.mskd.flux.core.network.tmdb.domain.model

sealed class TranslationRequest(val artworkId: Long, val language: String) {
    class Movie(artworkId: Long, language: String) : TranslationRequest(artworkId = artworkId, language = language)
    class Show(artworkId: Long, language: String) : TranslationRequest(artworkId = artworkId, language = language)
    class Episode(artworkId: Long, val season: Int, val number: Int, language: String) : TranslationRequest(artworkId = artworkId, language = language)
    class Season(artworkId: Long, val season: Int, language: String) : TranslationRequest(artworkId = artworkId, language = language)
}