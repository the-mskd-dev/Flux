package com.mskd.flux.screens.token

object TokenTestCases {

    data class SaveToken(
        val description: String,
        val apiResult: Any,
        val expectedMessage: TokenMessage,
        val expectedLoadCatalog: Boolean,
        val expectedNextButton: Boolean
    )

}