package com.kaem.flux

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaem.flux.screens.artwork.ArtworkScreen
import com.kaem.flux.screens.library.LibraryScreen
import com.kaem.flux.screens.permissions.PermissionsScreen
import com.kaem.flux.screens.permissions.fluxPermissionState
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {

            FluxTheme {

                val navController = rememberNavController()
                
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    navController = navController,
                    startDestination = "library"
                ) {

                    composable("library") {
                        LibraryScreen(
                            navigateToDetails = {
                                navController.navigate(
                                    route = "artwork/$it"
                                )
                            }
                        )
                    }

                    composable(
                        "artwork/{artworkId}",
                        arguments = listOf(navArgument("artworkId") { type = NavType.LongType }),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(Constants.Behaviour.TRANSITION_SPEED)
                            )
                        }
                    ) {
                        ArtworkScreen(
                            onBackButtonTap = {

                                if (navController.currentBackStackEntry != null)
                                    navController.popBackStack()

                            }
                        )
                    }

                }

            }

        }

    }

}