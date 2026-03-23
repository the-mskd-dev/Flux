package com.kaem.flux.screens.token

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.navigation.Route
import com.kaem.flux.screens.search.SearchViewModel
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.theme.AppTheme

@Composable
fun TokenScreen(
    onBack: () -> Unit,
    fromSettings: Boolean,
    viewModel: TokenViewModel = hiltViewModel<TokenViewModel, TokenViewModel.Factory>(
        creationCallback = { factory -> factory.create(fromSettings) }
    )
) {

    BackHandler(true) {
        if (fromSettings) onBack()
    }

    FluxScaffold(
        title = stringResource(R.string.tmdb_api_key),
        onBackTap = if (fromSettings) { { onBack() } } else null
    ) {



    }

}

@Preview
@Composable
fun TokenScreen_Preview() {
    AppTheme {
        TokenScreen(
            onBack = {},
            fromSettings = true
        )
    }
}
