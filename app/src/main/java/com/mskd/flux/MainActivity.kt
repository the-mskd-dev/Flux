package com.mskd.flux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Transition
import com.mskd.flux.screens.about.AboutScreen
import com.mskd.flux.screens.artwork.ArtworkScreen
import com.mskd.flux.screens.catalog.CatalogScreen
import com.mskd.flux.screens.customization.CustomizationScreen
import com.mskd.flux.screens.howTo.HowToScreen
import com.mskd.flux.screens.player.PlayerScreen
import com.mskd.flux.screens.search.SearchScreen
import com.mskd.flux.screens.settings.SettingsScreen
import com.mskd.flux.screens.setup.SetupScreen
import com.mskd.flux.screens.show.ShowScreen
import com.mskd.flux.screens.sources.SourcesScreen
import com.mskd.flux.screens.token.TokenScreen
import com.mskd.flux.screens.unknown.UnknownScreen
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.createColorScheme
import com.mskd.flux.utils.extensions.popScreen
import com.mskd.flux.utils.notificationsPermissionState
import com.mskd.flux.utils.storagePermissionState
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    val viewModel: MainViewModel by inject()
    val connectivityRepository: ConnectivityRepository by inject()

    private var onUserLeaveHintCallback: (() -> Unit)? = null

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            val settings by viewModel.settings.collectAsState()
            val customization by viewModel.customization.collectAsState()
            val storagePermission = storagePermissionState()
            val notificationsPermission = notificationsPermissionState()
            val isOnline by connectivityRepository.isOnline.collectAsState(false)

            LaunchedEffect(Unit) {
                if (notificationsPermission?.status?.isGranted == false && settings.externalPlayer) {
                    notificationsPermission.launchPermissionRequest()
                }
            }

            viewModel.disableSystemFoldersIfNeeded(permissionsGranted = storagePermission.status.isGranted)

            val startingScreen = viewModel.getStartingScreen()

            FluxTheme(
                isOnline = isOnline,
                customization = customization
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
                        entry<Route.Setup> {
                            SetupScreen(
                                navigate = { route ->
                                    backStack.clear()
                                    backStack.add(route)
                                },
                            )
                        }
                        entry<Route.Catalog> {
                            CatalogScreen(
                                navigate = { route -> backStack.add(route) },
                            )
                        }
                        entry<Route.Show> { entry ->
                            ShowScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.popScreen() },
                                artworkId = entry.artworkId,
                                colorScheme = createColorScheme(
                                    theme = customization.uiTheme,
                                    color = customization.color ?: entry.rgb
                                )
                            )
                        }
                        entry<Route.Artwork> { entry ->
                            ArtworkScreen(
                                navigate = { route -> backStack.add(route) },
                                onBack = { backStack.popScreen() },
                                artworkId = entry.artworkId,
                                season = entry.season,
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
                                fromSetup = entry.fromSetup
                            )
                        }
                        entry<Route.Sources> { entry ->
                            SourcesScreen(
                                navigate = { route ->
                                    backStack.clear()
                                    backStack.add(route)
                                },
                                fromSetup = entry.fromSetup,
                                onBack = { backStack.popScreen() },
                            )
                        }
                    }
                )

            }

        }

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (viewModel.settings.value.pipIsEnabled)
            onUserLeaveHintCallback?.invoke()

    }

    fun setOnUserLeaveHintCallback(callback: (() -> Unit)?) {
        onUserLeaveHintCallback = callback
    }

}