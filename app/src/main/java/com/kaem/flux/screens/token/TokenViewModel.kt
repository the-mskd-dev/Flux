package com.kaem.flux.screens.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.data.tmdb.token.TokenProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TokenViewModel.Factory::class)
class TokenViewModel @AssistedInject constructor(
    @Assisted val fromSettings: Boolean,
    private val tokenProvider: TokenProvider,
    private val tmdbService: TMDBService,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(fromSettings: Boolean): TokenViewModel
    }

    private val _event = MutableSharedFlow<TokenEvent>()
    val event = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(TokenUiState(showBackButton = fromSettings))
    val uiState: StateFlow<TokenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenProvider.getToken()?.let { setToken(it) }
        }
    }

    fun handleIntent(intent: TokenIntent) = viewModelScope.launch {
        when (intent) {
            is TokenIntent.SetToken -> setToken(intent.token)
            TokenIntent.SaveToken -> saveToken()
            TokenIntent.OnBackTap -> onBackTap()
            TokenIntent.OnNextTap -> onNextTap()
        }
    }

    private fun setToken(token: String) {
        _uiState.update { it.copy(token = token, message = TokenMessage.None) }
    }

    private suspend fun saveToken() {

        _uiState.update { it.copy(isLoading = true) }

        try {

            val authentication = tmdbService.authenticate()

            if (authentication.success) {

                tokenProvider.saveToken(_uiState.value.token)
                catalogRepository.loadCatalog()

                _uiState.update { it.copy(message = TokenMessage.Success, showBackButton = true) }

            } else {

                _uiState.update { it.copy(message = TokenMessage.Error, showBackButton = true) }

            }

        } catch (e: Exception) {

            e.printStackTrace()
            _uiState.update { it.copy(message = TokenMessage.Error, showBackButton = true) }

        }

        _uiState.update { it.copy(isLoading = false) }

    }

    private suspend fun onBackTap() {
        _event.emit(TokenEvent.BackToPreviousScreen)
    }

    private suspend fun onNextTap() {
        _event.emit(TokenEvent.NavigateToHomeScreen)
    }

}