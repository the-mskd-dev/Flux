package com.mskd.flux.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FluxScaffold(
    modifier: Modifier = Modifier,
    title: String,
    onBackTap: (() -> Unit)?,
    actions: @Composable (RowScope.() -> Unit) = {},
    snackbarHost: @Composable (() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    content: @Composable (PaddingValues) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        topBar = {

            CenterAlignedTopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = { Text.Headline.Medium(text = title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = actions,
                navigationIcon = {
                    onBackTap?.let {
                        BackButton(onTap = it)
                    }
                },
                scrollBehavior = scrollBehavior
            )

        }
    ) { innerPadding ->
        content(innerPadding)
    }
}