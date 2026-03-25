package com.kaem.flux.screens.token

import com.kaem.flux.model.tmdb.TMDBAuthentication

data class SaveTokenTestCase(
    val description: String,
    val apiResult: Any,
    val expectedMessage: TokenMessage,
    val expectedLoadCatalog: Boolean,
    val expectedEvent: TokenEvent?
)