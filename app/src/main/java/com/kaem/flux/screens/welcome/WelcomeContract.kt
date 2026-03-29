package com.kaem.flux.screens.welcome

import com.kaem.flux.R

data class WelcomeUiState(
    val page: WelcomePage = WelcomePage.WELCOME,
    val buttons: List<WelcomeButton> = listOf(WelcomeButton.NEXT),
)

enum class WelcomePage(val title: Int, val description: Int, val drawable: Int) {
    WELCOME(R.string.presentation_1_title, R.string.presentation_1_description, R.drawable.home_screen),
    PERMISSIONS(R.string.presentation_2_title, R.string.presentation_2_description, R.drawable.artwork_screen);

    companion object {
        val lastIndex = WelcomePage.entries.lastIndex
    }
}

enum class WelcomeButton {
    PREVIOUS, NEXT, PERMISSIONS
}

sealed class WelcomeIntent {
    data class OnPageChange(val pageIndex: Int): WelcomeIntent()
    data object OnPreviousTap: WelcomeIntent()
    data object OnNextTap: WelcomeIntent()
    data object OnPermissionTap: WelcomeIntent()
    data object OnPermissionGranted: WelcomeIntent()
}

sealed class WelcomeEvent {
    data class ScrollToPage(val pageIndex: Int): WelcomeEvent()
    data object NavigateToLibrary: WelcomeEvent()
    data object NavigateToToken: WelcomeEvent()
    data object OpenPermissionDialog: WelcomeEvent()
}