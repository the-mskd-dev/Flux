package com.kaem.flux.screens.welcome

import androidx.lifecycle.ViewModel
import com.kaem.flux.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WelcomeViewModel : ViewModel() {

    private val _event = MutableSharedFlow<WelcomeEvent>()
    val event = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    private val backgroundIds = listOf(
        R.drawable.home_screen,
        R.drawable.artwork_screen,
        R.drawable.search_screen
    )

    private val contentIds = listOf(
        R.string.presentation_1_title to R.string.presentation_1_description,
        R.string.presentation_2_title to R.string.presentation_2_description
    )

    fun handleIntent(intent: WelcomeIntent) {
        when (intent) {
            WelcomeIntent.OnPreviousTap -> onPreviousPage()
            WelcomeIntent.OnNextTap -> onNextPage()
            WelcomeIntent.OnPermissionTap -> onPermissionTap()
        }
    }

    private fun onNextPage() {
        _uiState.update {

            val index = (it.index + 1).coerceAtMost(contentIds.lastIndex)

            val buttons = buildList {
                add(WelcomeButton.PREVIOUS)
                add(if (index < contentIds.lastIndex) WelcomeButton.NEXT else WelcomeButton.PERMISSIONS)

            }

            it.copy(
                index = index,
                backgroundId = backgroundIds[index],
                contentIds = contentIds[index],
                buttons = buttons
            )

        }
    }

    private fun onPreviousPage() {
        _uiState.update {

            val index = (it.index - 1).coerceAtLeast(0)

            val buttons = buildList {
                if (index > 0) add(WelcomeButton.PREVIOUS)
            }

            it.copy(
                index = index,
                backgroundId = backgroundIds[index],
                contentIds = contentIds[index],
                buttons = buttons
            )

        }
    }

    private fun onPermissionTap() {
        TODO()
    }

}