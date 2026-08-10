package com.mskd.flux

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import com.mskd.flux.navigation.component.MainNavigationBar
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.Transition
import com.mskd.flux.navigation.domain.navigateToTab
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
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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

            val backStack = rememberNavBackStack(startingScreen)
            val currentRoute = backStack.lastOrNull() as? Route
            val showBottomBar = currentRoute.let {
                it is Route.Catalog || it is Route.Search || it is Route.Settings
            }

            var transitions by remember { mutableStateOf(Transition.Forward to Transition.Backward) }

            val navigate: (Route) -> Unit = { route ->
                transitions = Transition.Forward to Transition.Backward
                backStack.add(route)
            }
            val onBack: () -> Unit = {
                transitions = Transition.Forward to Transition.Backward
                backStack.popScreen()
            }

            FluxTheme(
                isOnline = isOnline,
                customization = customization
            ) {

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            MainNavigationBar(
                                currentTab = currentRoute,
                                onTabSelected = { target ->
                                    transitions = Transition.Fade to Transition.Fade
                                    navigateToTab(backStack, target)
                                },
                            )
                        }
                    }
                ) { _ ->

                    NavDisplay(
                        modifier = Modifier
                            .fillMaxSize(),
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        transitionSpec = { transitions.first },
                        popTransitionSpec = { transitions.second },
                        predictivePopTransitionSpec = { transitions.second },
                        entryProvider = entryProvider {
                            entry<Route.Setup> {
                                SetupScreen(
                                    navigate = { route ->
                                        backStack.clear()
                                        navigate(route)
                                    },
                                )
                            }
                            entry<Route.Catalog> {
                                CatalogScreen(
                                    navigate = { route -> navigate(route) },
                                )
                            }
                            entry<Route.Show> { entry ->
                                ShowScreen(
                                    navigate = { route -> navigate(route) },
                                    onBack = { onBack() },
                                    artworkId = entry.artworkId,
                                    colorScheme = createColorScheme(
                                        theme = customization.uiTheme,
                                        color = customization.color ?: entry.rgb
                                    )
                                )
                            }
                            entry<Route.Artwork> { entry ->
                                ArtworkScreen(
                                    navigate = { route -> navigate(route) },
                                    onBack = { onBack() },
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
                                    navigate = { route -> navigate(route) },
                                    onBack = { onBack() },
                                )
                            }
                            entry<Route.Search> { entry ->
                                SearchScreen(
                                    navigate = { route -> navigate(route) },
                                    onBack = { onBack() },
                                    withType = entry.withType,
                                    withGenre = entry.withGenre
                                )
                            }
                            entry<Route.Player> { entry ->
                                PlayerScreen(
                                    mediaId = entry.mediaId,
                                    onBack = { onBack() },
                                )
                            }
                            entry<Route.Settings> {
                                SettingsScreen(
                                    navigate = { route -> navigate(route) },
                                    onBack = { onBack() },
                                )
                            }
                            entry<Route.Customization> {
                                CustomizationScreen(
                                    onBack = { onBack() },
                                )
                            }
                            entry<Route.HowTo> {
                                HowToScreen(
                                    onBack = { onBack() }
                                )
                            }
                            entry<Route.About> {
                                AboutScreen(
                                    onBack = { onBack() }
                                )
                            }
                            entry<Route.Token> { entry ->
                                TokenScreen(
                                    onBack = { onBack() },
                                    navigate = { route ->
                                        backStack.clear()
                                        navigate(route)
                                    },
                                    fromSetup = entry.fromSetup
                                )
                            }
                            entry<Route.Sources> { entry ->
                                SourcesScreen(
                                    navigate = { route ->
                                        backStack.clear()
                                        navigate(route)
                                    },
                                    fromSetup = entry.fromSetup,
                                    onBack = { onBack() },
                                )
                            }
                        }
                    )

                }

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