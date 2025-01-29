package com.kaem.flux.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun FluxNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    route: String? = null,
    builder: NavGraphBuilder.() -> Unit
) : Unit = NavHost(
    modifier = modifier,
    navController = navController,
    startDestination = startDestination,
    contentAlignment = contentAlignment,
    route = route,
    builder = builder,
    enterTransition = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED)
        )
    },
    exitTransition = {
        fadeOut(animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED)) + scaleOut(targetScale = .8f, animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED))
    },
    popEnterTransition = {
        fadeIn(animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED))+ scaleIn(initialScale = .8f, animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED))
    },
    popExitTransition = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED)
        )
    }
)