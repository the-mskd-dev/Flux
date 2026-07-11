package com.mskd.flux.features.token.fake

import com.mskd.flux.features.token.domain.model.TokenMessage

object TokenTestCases {

    data class SaveToken(
        val description: String,
        val apiResult: Any,
        val expectedMessage: TokenMessage,
        val expectedLoadCatalog: Boolean,
    )

}