package com.kaem.flux.screens.token

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.navigation.Route
import com.kaem.flux.screens.search.SearchViewModel
import com.kaem.flux.ui.component.FluxScaffold

@Composable
fun TokenScreen(
    navigate: (Route) -> Unit,
    fromSettings: Boolean,
    viewModel: TokenViewModel = hiltViewModel<TokenViewModel, TokenViewModel.Factory>(
        creationCallback = { factory -> factory.create(fromSettings) }
    )
) {

    FluxScaffold(
        title = "",
        onBackTap = if (fromSettings) { {} } else null
    ) { }

}
