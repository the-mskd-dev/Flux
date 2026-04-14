package com.mskd.flux.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.tmdb.token.TokenRepository
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
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<WelcomeEvent>()
    val event = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

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

            val buttons = buildList {

                if (pageIndex > 0)
                    add(WelcomeButton.PREVIOUS)

                if (pageIndex < WelcomePage.lastIndex)
                    add(WelcomeButton.NEXT)

                if (pageIndex == WelcomePage.PERMISSIONS.ordinal)
                    add(WelcomeButton.PERMISSIONS)

            }

            it.copy(
                pageIndex = pageIndex,
                buttons = buttons
            )

        }
    }

    private suspend fun onNextPage() {
        val nextIndex = (_uiState.value.pageIndex + 1).coerceAtMost(WelcomePage.lastIndex)
        _event.emit(WelcomeEvent.ScrollToPage(nextIndex))
    }

    private suspend fun onPreviousPage() {
        val previousIndex = (_uiState.value.pageIndex - 1).coerceAtLeast(0)
        _event.emit(WelcomeEvent.ScrollToPage(previousIndex))
    }

    private suspend fun onPermissionGranted() {

        if (tokenRepository.tokenRequested) {
            _event.emit(WelcomeEvent.NavigateToToken)
        } else {
            _event.emit(WelcomeEvent.NavigateToLibrary)
        }

    }

}