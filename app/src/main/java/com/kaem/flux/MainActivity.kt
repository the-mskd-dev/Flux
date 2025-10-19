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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.screens.about.AboutScreen
import com.kaem.flux.screens.category.CategoryScreen
import com.kaem.flux.screens.home.HomeScreen
import com.kaem.flux.screens.howTo.HowToScreen
import com.kaem.flux.screens.media.MediaScreen
import com.kaem.flux.screens.search.SearchScreen
import com.kaem.flux.screens.settings.SettingsScreen
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.FluxNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var dataStoreRepository: DataStoreRepository
    private var uiTheme by mutableStateOf(Ui.THEME.SYSTEM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        observeDataStore()

        setContent {

            FluxTheme(theme = uiTheme) {

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
                                    route = "${Constants.Navigation.MEDIA}/$it"
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
                            navigateToHowTo = {
                                navController.navigate(
                                    route = Constants.Navigation.HOW_TO
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
                        "${Constants.Navigation.MEDIA}/{mediaId}",
                        arguments = listOf(navArgument("mediaId") { type = NavType.LongType }),
                    ) {
                        MediaScreen(
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
                                    route = "${Constants.Navigation.MEDIA}/$it"
                                )
                            }
                        )
                    }

                    composable(Constants.Navigation.SEARCH) {
                        SearchScreen(
                            onBackButtonTap = { navController.popBackStack() },
                            navigateToDetails = {
                                navController.navigate(
                                    route = "${Constants.Navigation.MEDIA}/$it"
                                )
                            }
                        )
                    }

                    composable(Constants.Navigation.SETTINGS) {
                        SettingsScreen(
                            onBackButtonTap = { navController.popBackStack() },
                            navigateToHowToScreen = {
                                navController.navigate(
                                    route = Constants.Navigation.HOW_TO
                                )
                            },
                            navigateToAboutScreen = {
                                navController.navigate(
                                    route = Constants.Navigation.ABOUT
                                )
                            }
                        )
                    }

                    composable(Constants.Navigation.HOW_TO) {
                        HowToScreen(
                            onBackButtonTap = { navController.popBackStack() }
                        )
                    }

                    composable(Constants.Navigation.ABOUT) {
                        AboutScreen(
                            onBackButtonTap = { navController.popBackStack() }
                        )
                    }

                }

            }

        }

    }

    private fun observeDataStore() {
        lifecycleScope.launch {
            dataStoreRepository.flow.collect {
                uiTheme = it.uiTheme
            }
        }
    }

}