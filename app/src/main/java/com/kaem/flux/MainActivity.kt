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
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.kaem.flux.screens.artwork.ArtworkScreen
import com.kaem.flux.screens.category.CategoryScreen
import com.kaem.flux.screens.home.HomeScreen
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.composableWithSlideTransition
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
                        HomeScreen(
                            navigateToDetails = {
                                navController.navigate(
                                    route = "artwork/$it"
                                )
                            },
                            navigateToCategory = {
                                navController.navigate(
                                    route = "category/${it.name}"
                                )
                            }
                        )
                    }

                    composableWithSlideTransition(
                        "artwork/{artworkId}",
                        arguments = listOf(navArgument("artworkId") { type = NavType.LongType }),
                    ) {
                        ArtworkScreen(
                            onBackButtonTap = {

                                if (navController.currentBackStackEntry != null)
                                    navController.popBackStack()

                            }
                        )
                    }

                    composableWithSlideTransition(
                        "category/{contentType}",
                        arguments = listOf(navArgument("contentType") { type = NavType.StringType }),
                    ) {
                        CategoryScreen()
                    }

                }

            }

        }

    }

}