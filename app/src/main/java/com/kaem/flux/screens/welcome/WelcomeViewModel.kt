package com.kaem.flux.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.R
import com.kaem.flux.data.tmdb.token.TokenProvider
import com.kaem.flux.screens.home.HomeEvent
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
            WelcomeIntent.OnPreviousTap -> onPreviousPage()
            WelcomeIntent.OnNextTap -> onNextPage()
            WelcomeIntent.OnPermissionTap -> _event.emit(WelcomeEvent.OpenPermissionDialog)
            WelcomeIntent.OnPermissionGranted -> onPermissionGranted()
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

    private suspend fun onPermissionGranted() {

        val token = tokenProvider.getToken()

        if (token.isNullOrBlank()) {
            _event.emit(WelcomeEvent.NavigateToToken)
        } else {
            _event.emit(WelcomeEvent.NavigateToLibrary)
        }


    }

}