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

    private val drawables = listOf(
        R.drawable.home_screen,
        R.drawable.artwork_screen,
        R.drawable.search_screen
    )

    fun handleIntent(intent: WelcomeIntent) {
        when (intent) {
            WelcomeIntent.OnPermissionTap -> TODO()
            is WelcomeIntent.SelectPage -> selectPage(page = intent.page)
        }
    }

    private fun selectPage(page: Int) {
        _uiState.update {
            it.copy(
                page = page,
                backgroundImage = drawables[page]
            )
        }
    }

}