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
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaem.flux.Navigation.Navigation
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
                    startDestination = Navigation.LIBRARY.route
                ) {

                    composable(
                        route = Navigation.LIBRARY.route,
                        arguments = Navigation.LIBRARY.arguments
                    ) {
                        HomeScreen(
                            navigate = { route -> navController.navigate(route) },
                        )
                    }

                    composable(
                        route = Navigation.MEDIA.route,
                        arguments = Navigation.MEDIA.arguments,
                    ) {
                        MediaScreen(
                            onBackButtonTap = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Navigation.CATEGORY.route,
                        arguments = Navigation.CATEGORY.arguments,
                    ) {
                        CategoryScreen(
                            onBackButtonTap = { navController.popBackStack() },
                            navigateToDetails = {
                                navController.navigate(
                                    route = "${Navigation.MEDIA.route}/$it"
                                )
                            }
                        )
                    }

                    composable(
                        route = Navigation.SEARCH.route,
                        arguments = Navigation.SEARCH.arguments
                    ) {
                        SearchScreen(
                            onBackButtonTap = { navController.popBackStack() },
                            navigateToDetails = {
                                navController.navigate(
                                    route = "${Navigation.MEDIA.route}/$it"
                                )
                            }
                        )
                    }

                    composable(
                        route = Navigation.SETTINGS.route,
                        arguments = Navigation.SETTINGS.arguments
                    ) {
                        SettingsScreen(
                            onBackButtonTap = { navController.popBackStack() },
                            navigateToHowToScreen = {
                                navController.navigate(
                                    route = Navigation.HOW_TO.route
                                )
                            },
                            navigateToAboutScreen = {
                                navController.navigate(
                                    route = Navigation.ABOUT.route
                                )
                            }
                        )
                    }

                    composable(
                        route = Navigation.HOW_TO.route,
                        arguments = Navigation.HOW_TO.arguments
                    ) {
                        HowToScreen(
                            onBackButtonTap = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Navigation.ABOUT.route,
                        arguments = Navigation.ABOUT.arguments
                    ) {
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