package com.kaem.flux.screens.welcome

object WelcomeTestCases {

    data class OnPermissionGranted(
        val description: String,
        val hasToken: Boolean,
        val expectedEvent: WelcomeEvent
    )

}