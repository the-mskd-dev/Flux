package com.mskd.flux.ui.component.global

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mskd.flux.ui.theme.FluxUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxScaffold(
    modifier: Modifier = Modifier,
    title: String?,
    onBackTap: (() -> Unit)?,
    actions: @Composable (RowScope.() -> Unit) = {},
    snackbarHost: @Composable (() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    topAppBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
    ),
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.pinnedScrollBehavior(),
    content: @Composable (PaddingValues) -> Unit
) {

    Scaffold(
        modifier = Modifier.then(scrollBehavior?.let { Modifier.nestedScroll(it.nestedScrollConnection) } ?: Modifier),
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        topBar = {

            FluxTopAppBar(
                modifier = modifier,
                title = title,
                colors = topAppBarColors,
                actions = actions,
                onBackTap = onBackTap,
                scrollBehavior = scrollBehavior
            )

        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxTopAppBar(
    modifier: Modifier = Modifier,
    title: String?,
    actions: @Composable (RowScope.() -> Unit) = {},
    onBackTap: (() -> Unit)? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior:  TopAppBarScrollBehavior? = null
) {

    CenterAlignedTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {

            Text.TopBar.Title(
                modifier = Modifier
                    .padding(vertical = FluxUI.Space.extraSmall)
                    .then(modifier),
                text = title
            )

        },
        colors = colors,
        actions = actions,
        navigationIcon = {
            onBackTap?.let {
                IconButton(
                    onClick = { it() },
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back button"
                        )
                    }
                )
            }
        },
        scrollBehavior = scrollBehavior
    )

}