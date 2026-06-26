package com.mskd.flux.screens.token

import com.mskd.flux.screen.token.TokenMessage

object TokenTestCases {

    data class SaveToken(
        val description: String,
        val apiResult: Any,
        val expectedMessage: TokenMessage,
        val expectedLoadCatalog: Boolean,
    )

}