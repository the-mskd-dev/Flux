package com.kaem.flux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaem.flux.navigation.Route
import com.kaem.flux.navigation.Transition
import com.kaem.flux.screens.about.AboutScreen
import com.kaem.flux.screens.artwork.ArtworkScreen
import com.kaem.flux.screens.home.HomeScreen
import com.kaem.flux.screens.howTo.HowToScreen
import com.kaem.flux.screens.player.PlayerScreen
import com.kaem.flux.screens.search.SearchScreen
import com.kaem.flux.screens.settings.SettingsScreen
import com.kaem.flux.screens.token.TokenScreen
import com.kaem.flux.screens.welcome.WelcomeScreen
import com.kaem.flux.screens.welcome.fluxPermissionState
import com.kaem.flux.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var viewModel: MainViewModel

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val permissions = fluxPermissionState()

            val startingScreen = viewModel.getStartingScreen(permissions.status.isGranted)

            AppTheme(theme = settings.uiTheme) {

                val backStack = rememberNavBackStack(startingScreen)

                NavDisplay(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    transitionSpec = { Transition.Forward },
                    popTransitionSpec = { Transition.Backward },
                    predictivePopTransitionSpec = { Transition.Backward },
                    entryProvider = entryProvider {
                        entry<Route.Welcome> {
                            WelcomeScreen(
                                navigate = { route ->
                                    backStack.clear()
                                    backStack.add(route)
                                },
                            )
                        }
                        entry<Route.Library> {
                            HomeScreen(
                                navigate = { route -> backStack.add(route) },
                            )
                        }
                        entry<Route.Artwork> { entry ->
                            ArtworkScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.removeLastOrNull() },
                                mediaId = entry.artworkId
                            )
                        }
                        entry<Route.Search> { entry ->
                            SearchScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.removeLastOrNull() },
                                contentType = entry.contentType
                            )
                        }
                        entry<Route.Player> { entry ->
                            PlayerScreen(
                                mediaId = entry.mediaId,
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
                        entry<Route.Token> { entry ->
                            TokenScreen(
                                onBack = { backStack.removeLastOrNull() },
                                navigate = { route ->
                                    backStack.clear()
                                    backStack.add(route)
                                },
                                fromSettings = entry.fromSettings
                            )
                        }
                    }
                )

            }

        }

    }

}