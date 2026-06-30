package com.mskd.flux.screen.sources

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SourcesViewModel() : ViewModel() {

    //region State

    private val _uiState = MutableStateFlow(SourcesUiState())
    val uiState = _uiState.asStateFlow()

    //endregion

    //region Public Methods

    //endregion

    //region Private Methods

    //endregion

}