package com.kaem.flux.screens.search

sealed class SearchIntent {
    object OnBackTap: SearchIntent()
    data class OnMediaTap(val mediaId: Long): SearchIntent()
    data class DoSearch(val query: String) : SearchIntent()
}