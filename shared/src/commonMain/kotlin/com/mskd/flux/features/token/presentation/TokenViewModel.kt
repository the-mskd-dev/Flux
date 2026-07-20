package com.mskd.flux.features.token.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.features.token.domain.model.AuthenticateResult
import com.mskd.flux.features.token.domain.model.TokenMessage
import com.mskd.flux.features.token.domain.usecase.SaveTokenAndSyncUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TokenViewModel(
    fromSetup: Boolean,
    private val tokenDataStore: TokenDataStore,
    private val saveTokenAndSyncUseCase: SaveTokenAndSyncUseCase,
    private val appInfo: AppInfo
) : ViewModel() {

    private val _event = MutableSharedFlow<TokenEvent>()
    val event = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(TokenUiState(showBackButton = !fromSetup))
    val uiState: StateFlow<TokenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val token = tokenDataStore.getToken().ifBlank { appInfo.debugToken }
            setToken(token)
        }
    }

    fun handleIntent(intent: TokenIntent) = viewModelScope.launch {
        when (intent) {
            is TokenIntent.SetToken -> setToken(intent.token)
            TokenIntent.SaveToken -> saveToken()
            TokenIntent.OnBackTap -> onBackTap()
            TokenIntent.OnCancelTap -> onCancelTap()
            TokenIntent.OnNextTap -> onNextTap()
        }
    }

    private fun setToken(token: String) {
        _uiState.update { it.copy(token = token, message = TokenMessage.None) }
    }

    private suspend fun saveToken() {

        _uiState.update { it.copy(isLoading = true) }

        val authenticateResult = saveTokenAndSyncUseCase(token = _uiState.value.token)

        when (authenticateResult) {
            AuthenticateResult.SUCCESS -> {
                if (_uiState.value.showBackButton) {
                    _uiState.update {
                        it.copy(
                            message = TokenMessage.Success,
                            isLoading = false
                        )
                    }
                } else onNextTap()
            }
            AuthenticateResult.FAILURE -> {
                _uiState.update {
                    it.copy(
                        message = TokenMessage.Error,
                        isLoading = false
                    )
                }
            }
        }

    }

    private suspend fun onBackTap() {
        _event.emit(TokenEvent.BackToPreviousScreen)
    }

    private suspend fun onCancelTap() {
        tokenDataStore.dontRequestToken()
        _event.emit(TokenEvent.NavigateToCatalogScreen)
    }

    private suspend fun onNextTap() {
        _event.emit(TokenEvent.NavigateToCatalogScreen)
    }

}