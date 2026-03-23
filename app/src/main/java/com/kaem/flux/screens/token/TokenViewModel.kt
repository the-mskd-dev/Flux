package com.kaem.flux.screens.token

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.tmdb.token.TokenProvider
import com.kaem.flux.screens.artwork.ArtworkViewModel
import com.kaem.flux.screens.settings.SettingsUiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel(assistedFactory = TokenViewModel.Factory::class)
class TokenViewModel @AssistedInject constructor(
    @Assisted val fromSettings: Boolean,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(fromSettings: Boolean): TokenViewModel
    }

    private val _event = MutableSharedFlow<TokenEvent>()
    val event = _event.asSharedFlow()

    private val _token = MutableStateFlow("")

    val uiState: StateFlow<TokenUiState> = _token.map { token ->
        TokenUiState(
            token = token,
            showBackButton = fromSettings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TokenUiState()
    )

    init {
        tokenProvider.flow.map {
            setToken(it.orEmpty())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TokenUiState()
        )
    }

    fun handleIntent(intent: TokenIntent) = viewModelScope.launch {
        when (intent) {
            is TokenIntent.SetToken -> setToken(intent.token)
            TokenIntent.SaveToken -> saveToken()
            TokenIntent.OnBackTap -> onBackTap()
            TokenIntent.TapOnGetToken -> tapOnGetToken()
            TokenIntent.TapOnTMDB -> tapOnTMDB()
        }
    }

    private fun setToken(token: String) {
        _token.update { token }
    }

    private suspend fun saveToken() {
        tokenProvider.saveToken(_token.value)
    }

    private suspend fun onBackTap() {
        _event.emit(TokenEvent.BackToPreviousScreen)
    }

    private suspend fun tapOnGetToken() {
        _event.emit(TokenEvent.NavigateToGetToken)
    }

    private suspend fun tapOnTMDB() {
        _event.emit(TokenEvent.NavigateToTMDB)
    }

}