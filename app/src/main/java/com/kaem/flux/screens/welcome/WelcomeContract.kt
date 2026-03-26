package com.kaem.flux.screens.welcome

import com.kaem.flux.R

data class WelcomeUiState(
    val index: Int = 0,
    val backgroundId: Int = R.drawable.home_screen,
    val contentIds: Pair<Int, Int> = Pair(R.string.presentation_1_title, R.string.presentation_1_description),
    val buttons: List<WelcomeButton> = listOf(WelcomeButton.NEXT)
)

enum class WelcomeButton {
    PREVIOUS, NEXT, PERMISSIONS
}

sealed class WelcomeIntent {
    data object OnPreviousTap: WelcomeIntent()
    data object OnNextTap: WelcomeIntent()
    data object OnPermissionTap: WelcomeIntent()
    data object OnPermissionGranted: WelcomeIntent()
}

sealed class WelcomeEvent {
    data object NavigateToLibrary: WelcomeEvent()
    data object NavigateToToken: WelcomeEvent()
    data object OpenPermissionDialog: WelcomeEvent()
}