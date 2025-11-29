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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.data.repository.SettingsPreferences
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.data.repository.settingsDatastore
import com.kaem.flux.navigation.Route
import com.kaem.flux.navigation.Transition
import com.kaem.flux.screens.about.AboutScreen
import com.kaem.flux.screens.category.CategoryScreen
import com.kaem.flux.screens.home.HomeScreen
import com.kaem.flux.screens.howTo.HowToScreen
import com.kaem.flux.screens.media.MediaScreen
import com.kaem.flux.screens.search.SearchScreen
import com.kaem.flux.screens.settings.SettingsScreen
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {

            val settings by settingsRepository.settingsPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = SettingsPreferences(),
                lifecycleOwner = LocalLifecycleOwner.current
            )

            FluxTheme(theme = settings.uiTheme) {

                val backStack = rememberNavBackStack(Route.Library)

                NavDisplay(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    transitionSpec = { Transition.Forward },
                    popTransitionSpec = { Transition.Backward },
                    predictivePopTransitionSpec = { Transition.Backward },
                    entryProvider = entryProvider {
                        entry<Route.Library> {
                            HomeScreen(
                                navigate = { route -> backStack.add(route) },
                            )
                        }
                        entry<Route.Media> { entry ->
                            MediaScreen(
                                onBack = { backStack.removeLastOrNull() },
                                mediaId = entry.mediaId
                            )
                        }
                        entry<Route.Category> { entry ->
                            CategoryScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.removeLastOrNull() },
                                contentType = entry.contentType
                            )
                        }
                        entry<Route.Search> {
                            SearchScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<Route.Settings> {
                            SettingsScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<Route.HowTo> {
                            HowToScreen(
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<Route.About> {
                            AboutScreen(
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )

            }

        }

    }

}