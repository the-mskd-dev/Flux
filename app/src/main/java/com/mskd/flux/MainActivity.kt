package com.mskd.flux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
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
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Transition
import com.mskd.flux.screens.about.AboutScreen
import com.mskd.flux.screens.artwork.ArtworkScreen
import com.mskd.flux.screens.customization.CustomizationScreen
import com.mskd.flux.screens.home.HomeScreen
import com.mskd.flux.screens.howTo.HowToScreen
import com.mskd.flux.screens.player.PlayerScreen
import com.mskd.flux.screens.search.SearchScreen
import com.mskd.flux.screens.settings.SettingsScreen
import com.mskd.flux.screens.token.TokenScreen
import com.mskd.flux.screens.unknown.UnknownScreen
import com.mskd.flux.screens.welcome.WelcomeScreen
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.ui.theme.createColorScheme
import com.mskd.flux.utils.extensions.popScreen
import com.mskd.flux.utils.notificationsPermissionState
import com.mskd.flux.utils.storagePermissionState
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
            val customization by viewModel.customization.collectAsStateWithLifecycle()
            val storagePermission = storagePermissionState()
            val notificationsPermission = notificationsPermissionState()

            LaunchedEffect(Unit) {
                if (notificationsPermission?.status?.isGranted == false && settings.externalPlayer) {
                    notificationsPermission.launchPermissionRequest()
                }
            }

            val startingScreen = viewModel.getStartingScreen(storagePermission.status.isGranted)

            AppTheme(
                theme = customization.uiTheme,
                color = customization.color
            ) {

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
                                onBack = { backStack.popScreen() },
                                artworkId = entry.artworkId,
                                colorScheme = createColorScheme(
                                    theme = customization.uiTheme,
                                    color = customization.color ?: entry.rgb
                                )
                            )
                        }
                        entry<Route.UnknownArtworks> {
                            UnknownScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.popScreen() },
                            )
                        }
                        entry<Route.Search> { entry ->
                            SearchScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.popScreen() },
                                contentType = entry.contentType
                            )
                        }
                        entry<Route.Player> { entry ->
                            PlayerScreen(
                                mediaId = entry.mediaId,
                                onBack = { backStack.popScreen() },
                            )
                        }
                        entry<Route.Settings> {
                            SettingsScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.popScreen() },
                            )
                        }
                        entry<Route.Customization> {
                            CustomizationScreen(
                                onBack = { backStack.popScreen() },
                            )
                        }
                        entry<Route.HowTo> {
                            HowToScreen(
                                onBack = { backStack.popScreen() }
                            )
                        }
                        entry<Route.About> {
                            AboutScreen(
                                onBack = { backStack.popScreen() }
                            )
                        }
                        entry<Route.Token> { entry ->
                            TokenScreen(
                                onBack = { backStack.popScreen() },
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