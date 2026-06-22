package com.mskd.flux.screen.welcome

import com.mskd.flux.R
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.presentation_1_description
import flux.shared.generated.resources.presentation_1_title
import flux.shared.generated.resources.presentation_2_description
import flux.shared.generated.resources.presentation_2_title
import org.jetbrains.compose.resources.StringResource

data class WelcomeUiState(
    val pageIndex: Int = 0,
    val buttons: List<WelcomeButton> = listOf(WelcomeButton.NEXT),
)

enum class WelcomePage(val titleId: StringResource, val descriptionId: StringResource, val drawableId: Int) {
    WELCOME(Res.string.presentation_1_title, Res.string.presentation_1_description, R.drawable.home_screen),
    PERMISSIONS(Res.string.presentation_2_title, Res.string.presentation_2_description, R.drawable.artwork_screen);

    companion object {
        val lastIndex = entries.lastIndex
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