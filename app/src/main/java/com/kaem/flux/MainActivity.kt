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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.navigation.Route
import com.kaem.flux.screens.about.AboutScreen
import com.kaem.flux.screens.category.CategoryScreen
import com.kaem.flux.screens.home.HomeScreen
import com.kaem.flux.screens.howTo.HowToScreen
import com.kaem.flux.screens.media.MediaScreen
import com.kaem.flux.screens.search.SearchScreen
import com.kaem.flux.screens.settings.SettingsScreen
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
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

                val backStack = remember { mutableStateListOf<Any>(Route.Library) }

                NavDisplay(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is Route.Library -> NavEntry(key) {
                                HomeScreen(
                                    navigate = { route -> backStack.add(route) },
                                )
                            }
                            is Route.Media -> NavEntry(key) {
                                MediaScreen(
                                    onBack = { backStack.removeLastOrNull() },
                                    mediaId = key.mediaId
                                )
                            }
                            is Route.Category -> NavEntry(key) {
                                CategoryScreen(
                                    navigate = { route -> backStack.add(route) },
                                    onBack = { backStack.removeLastOrNull() },
                                    contentType = key.contentType
                                )
                            }
                            is Route.Search -> NavEntry(key) {
                                SearchScreen(
                                    navigate = { route -> backStack.add(route) },
                                    onBack = { backStack.removeLastOrNull() },
                                )
                            }
                            is Route.Settings -> NavEntry(key) {
                                SettingsScreen(
                                    navigate = { route -> backStack.add(route) },
                                    onBack = { backStack.removeLastOrNull() },
                                )
                            }
                            is Route.HowTo -> NavEntry(key) {
                                HowToScreen(
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            is Route.About -> NavEntry(key) {
                                AboutScreen(
                                    onBack = { backStack.removeLastOrNull() }
                                )
                            }
                            else -> NavEntry(Unit) { Text.Display.Large("Unknown route") }
                        }
                    }
                )

                /*val navController = rememberNavController()
                
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
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Navigation.CATEGORY.route,
                        arguments = Navigation.CATEGORY.arguments,
                    ) {
                        CategoryScreen(
                            navigate = { route -> navController.navigate(route) },
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = Navigation.SEARCH.route,
                        arguments = Navigation.SEARCH.arguments
                    ) {
                        SearchScreen(
                            navigate = { route -> navController.navigate(route) },
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = Navigation.SETTINGS.route,
                        arguments = Navigation.SETTINGS.arguments
                    ) {
                        SettingsScreen(
                            navigate = { route -> navController.navigate(route) },
                            onBack = { navController.popBackStack() },
                        )
                    }

                    composable(
                        route = Navigation.HOW_TO.route,
                        arguments = Navigation.HOW_TO.arguments
                    ) {
                        HowToScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Navigation.ABOUT.route,
                        arguments = Navigation.ABOUT.arguments
                    ) {
                        AboutScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                }*/

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