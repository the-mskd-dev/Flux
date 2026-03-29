package com.kaem.flux.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.R
import com.kaem.flux.data.tmdb.token.TokenProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val tokenProvider: TokenProvider
) : ViewModel() {

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

    fun handleIntent(intent: WelcomeIntent) = viewModelScope.launch {
        when (intent) {
            is WelcomeIntent.OnPageChange -> onPageChange(pageIndex = intent.pageIndex)
            WelcomeIntent.OnPreviousTap -> onPreviousPage()
            WelcomeIntent.OnNextTap -> onNextPage()
            WelcomeIntent.OnPermissionTap -> _event.emit(WelcomeEvent.OpenPermissionDialog)
            WelcomeIntent.OnPermissionGranted -> onPermissionGranted()
        }
    }

    private fun onPageChange(pageIndex: Int) {
        _uiState.update {

            val page = WelcomePage.entries[pageIndex]

            val buttons = buildList {

                if (pageIndex > 0)
                    add(WelcomeButton.PREVIOUS)

                if (pageIndex < contentIds.lastIndex)
                    add(WelcomeButton.NEXT)

                if (page == WelcomePage.PERMISSIONS)
                    add(WelcomeButton.PERMISSIONS)

            }

            it.copy(
                page = page,
                buttons = buttons
            )

        }
    }

    private suspend fun onNextPage() {
        val nextIndex = (_uiState.value.page.ordinal + 1).coerceAtMost(WelcomePage.lastIndex)
        _event.emit(WelcomeEvent.ScrollToPage(nextIndex))
    }

    private suspend fun onPreviousPage() {
        val previousIndex = (_uiState.value.page.ordinal - 1).coerceAtLeast(0)
        _event.emit(WelcomeEvent.ScrollToPage(previousIndex))
    }

    private suspend fun onPermissionGranted() {

        if (tokenProvider.hasToken) {
            _event.emit(WelcomeEvent.NavigateToLibrary)
        } else {
            _event.emit(WelcomeEvent.NavigateToToken)
        }

    }

}