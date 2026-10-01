package com.mskd.flux.ui.component.global

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mskd.flux.presentation.FluxUI
import com.mskd.flux.presentation.Text
import com.mskd.flux.presentation.animateAlphaState
import com.mskd.flux.presentation.blurBackground
import com.mskd.flux.presentation.blurForeground
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxScaffold(
    title: String?,
    animatedTitle: Boolean = false,
    showBackButton: Boolean = true,
    onBackTap: (() -> Unit) = { },
    actions: @Composable (RowScope.() -> Unit) = {},
    snackbarHost: @Composable (() -> Unit) = {},
    floatingActionButton: @Composable (() -> Unit) = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = MaterialTheme.colorScheme.background,
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.pinnedScrollBehavior(),
    content: @Composable (PaddingValues) -> Unit
) {

    val hazeState = rememberHazeState()
    val isScrolled by remember {
        derivedStateOf {
            scrollBehavior?.let { if (it.state.contentOffset < -10f) 1f else 0f } ?: 1f
        }
    }
    val animatedAlpha by animateAlphaState(targetValue = isScrolled,)

    Scaffold(
        modifier = Modifier.then(scrollBehavior?.let { Modifier.nestedScroll(it.nestedScrollConnection) } ?: Modifier),
        snackbarHost = snackbarHost,
        containerColor = containerColor,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        topBar = {

            FluxTopAppBar(
                modifier = Modifier.blurForeground(
                    state = hazeState,
                    alpha = animatedAlpha,
                ),
                title = title,
                titleGraphicsLayer = { alpha = if (animatedTitle) animatedAlpha else 1f },
                actions = actions,
                onBackTap = if (showBackButton) onBackTap else null,
                scrollBehavior = scrollBehavior
            )

        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blurBackground(hazeState)
        ) {
            content(innerPadding)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxTopAppBar(
    modifier: Modifier = Modifier,
    title: String?,
    titleGraphicsLayer: GraphicsLayerScope.() -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    onBackTap: (() -> Unit)? = null,
    scrollBehavior:  TopAppBarScrollBehavior? = null
) {

    CenterAlignedTopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = {

            Text.TopBar.Title(
                modifier = Modifier
                    .padding(vertical = FluxUI.Space.extraSmall)
                    .graphicsLayer(titleGraphicsLayer),
                text = title
            )

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
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