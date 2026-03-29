package com.kaem.flux.screens.token

import com.kaem.flux.model.tmdb.TMDBAuthentication

object TokenTestCases {

    data class SaveToken(
        val description: String,
        val apiResult: Any,
        val expectedMessage: TokenMessage,
        val expectedLoadCatalog: Boolean,
        val expectedNextButton: Boolean
    )

}