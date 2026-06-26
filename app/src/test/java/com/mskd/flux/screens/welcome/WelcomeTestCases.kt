package com.mskd.flux.screens.welcome

import com.mskd.flux.screen.welcome.WelcomeEvent

object WelcomeTestCases {

    data class OnPermissionGranted(
        val description: String,
        val tokenRequested: Boolean,
        val expectedEvent: WelcomeEvent
    )

}