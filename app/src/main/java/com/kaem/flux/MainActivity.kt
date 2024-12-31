package com.kaem.flux

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaem.flux.screens.artwork.ArtworkScreen
import com.kaem.flux.screens.category.CategoryScreen
import com.kaem.flux.screens.home.HomeScreen
import com.kaem.flux.screens.search.SearchScreen
import com.kaem.flux.screens.settings.SettingsScreen
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.FluxNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {

            FluxTheme {

                val navController = rememberNavController()
                
                FluxNavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    navController = navController,
                    startDestination = Constants.Navigation.LIBRARY
                ) {

                    composable(Constants.Navigation.LIBRARY) {
                        HomeScreen(
                            navigateToDetails = {
                                navController.navigate(
                                    route = "${Constants.Navigation.ARTWORK}/$it"
                                )
                            },
                            navigateToCategory = {
                                navController.navigate(
                                    route = "${Constants.Navigation.CATEGORY}/${it.name}"
                                )
                            },
                            navigateToSearch = {
                                navController.navigate(
                                    route = Constants.Navigation.SEARCH
                                )
                            },
                            navigateToSettings = {
                                navController.navigate(
                                    route = Constants.Navigation.SETTINGS
                                )
                            }
                        )
                    }

                    composable(
                        "${Constants.Navigation.ARTWORK}/{artworkId}",
                        arguments = listOf(navArgument("artworkId") { type = NavType.LongType }),
                    ) {
                        ArtworkScreen(
                            onBackButtonTap = { navController.popBackStack() }
                        )
                    }

                    composable(
                        "${Constants.Navigation.CATEGORY}/{contentType}",
                        arguments = listOf(navArgument("contentType") { type = NavType.StringType }),
                    ) {
                        CategoryScreen(
                            onBackButtonTap = { navController.popBackStack() },
                            navigateToDetails = {
                                navController.navigate(
                                    route = "${Constants.Navigation.ARTWORK}/$it"
                                )
                            }
                        )
                    }

                    composable(Constants.Navigation.SEARCH) {
                        SearchScreen(
                            onBackButtonTap = { navController.popBackStack() },
                            navigateToDetails = {
                                navController.navigate(
                                    route = "${Constants.Navigation.ARTWORK}/$it"
                                )
                            }
                        )
                    }

                    composable(Constants.Navigation.SETTINGS) {
                        SettingsScreen(
                            onBackButtonTap = { navController.popBackStack() }
                        )
                    }

                }

            }

        }

    }

}