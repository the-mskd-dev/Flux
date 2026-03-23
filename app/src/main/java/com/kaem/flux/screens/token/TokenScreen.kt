package com.kaem.flux.screens.token

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.navigation.Route
import com.kaem.flux.screens.search.SearchViewModel
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.theme.AppTheme

@Composable
fun TokenScreen(
    navigate: (Route) -> Unit,
    fromSettings: Boolean,
    viewModel: TokenViewModel = hiltViewModel<TokenViewModel, TokenViewModel.Factory>(
        creationCallback = { factory -> factory.create(fromSettings) }
    )
) {

    FluxScaffold(
        title = "TMDB Token",
        onBackTap = if (fromSettings) { {} } else null
    ) {

        

    }

}

@Preview
@Composable
fun TokenScreen_Preview() {
    AppTheme {
        TokenScreen(
            navigate = {},
            fromSettings = true
        )
    }
}
