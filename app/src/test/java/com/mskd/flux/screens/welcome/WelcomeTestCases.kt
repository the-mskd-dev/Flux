package com.mskd.flux.screens.welcome

import com.mskd.flux.features.welcome.presentation.WelcomeEvent

object WelcomeTestCases {

    data class OnPermissionGranted(
        val description: String,
        val tokenRequested: Boolean,
        val expectedEvent: WelcomeEvent
    )

}