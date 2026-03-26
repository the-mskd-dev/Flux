package com.kaem.flux.screens.welcome

import androidx.annotation.StringRes
import com.kaem.flux.R

data class WelcomeUiState(
    val page: Int = 0,
    val backgroundImage: Int = R.drawable.home_screen
)

sealed class WelcomeIntent {
    data class SelectPage(val page: Int): WelcomeIntent()
    data object OnPermissionTap: WelcomeIntent()
}

sealed class WelcomeEvent {
    data object NavigateToLibrary: WelcomeEvent()
    data object NavigateToToken: WelcomeEvent()
    data object OpenPermissionDialog: WelcomeEvent()
}