package com.mskd.flux.screens.welcome

object WelcomeTestCases {

    data class OnPermissionGranted(
        val description: String,
        val tokenRequested: Boolean,
        val expectedEvent: WelcomeEvent
    )

}