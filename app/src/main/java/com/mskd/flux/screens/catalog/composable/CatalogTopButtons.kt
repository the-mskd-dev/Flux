package com.mskd.flux.screens.catalog.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_flux
import org.jetbrains.compose.resources.painterResource

private enum class IconRefreshState { Idle, Dragging, Refreshing }


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatalogTopButtons() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FluxUI.Space.small),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(40.dp),
            painter = painterResource(Res.drawable.ic_flux),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Flux icon"
        )
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatalogLoadingIndicator(
    modifier: Modifier = Modifier,
    pullToRefreshState: PullToRefreshState,
    isRefreshing: Boolean,
) {

    val iconState = when {
        isRefreshing -> IconRefreshState.Refreshing
        pullToRefreshState.distanceFraction > 0f -> IconRefreshState.Dragging
        else -> IconRefreshState.Idle
    }

    AnimatedContent(
        targetState = iconState,
        transitionSpec = {
            fadeIn(tween(100)) + scaleIn(initialScale = 0.6f) togetherWith
                    fadeOut(tween(100)) + scaleOut(targetScale = 0.6f)
        }
    ) { state ->

        when (state) {
            IconRefreshState.Idle -> {
                Icon(
                    modifier = modifier,
                    painter = painterResource(Res.drawable.ic_flux),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Flux icon"
                )
            }
            IconRefreshState.Dragging -> {
                LoadingIndicator(
                    modifier = modifier,
                    progress = { pullToRefreshState.distanceFraction.coerceIn(0f, 1f) },
                )
            }
            IconRefreshState.Refreshing -> {
                ContainedLoadingIndicator(modifier = modifier)
            }
        }

    }

}