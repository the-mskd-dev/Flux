package com.mskd.flux

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mskd.flux.app.AppViewModel
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.ui.FluxNavigation
import com.mskd.flux.screens.artwork.ArtworkScreen
import com.mskd.flux.screens.catalog.CatalogScreen
import com.mskd.flux.screens.player.PlayerScreen
import com.mskd.flux.screens.search.SearchScreen
import com.mskd.flux.screens.sources.SourcesScreen
import com.mskd.flux.screens.unknown.UnknownScreen
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.createColorScheme
import com.mskd.flux.utils.rememberNotificationsPermission
import com.mskd.flux.utils.rememberStoragePermission
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    val viewModel: AppViewModel by inject()
    val connectivityRepository: ConnectivityRepository by inject()

    private var onUserLeaveHintCallback: (() -> Unit)? = null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            val settings by viewModel.settings.collectAsState()
            val customization by viewModel.customization.collectAsState()
            val storagePermission = rememberStoragePermission()
            val notificationsPermission = rememberNotificationsPermission()
            val isOnline by connectivityRepository.isOnline.collectAsState(false)

            LaunchedEffect(Unit) {
                if (notificationsPermission?.isGranted == false && settings.externalPlayer) {
                    notificationsPermission.request()
                }
            }

            viewModel.disableSystemFoldersIfNeeded(permissionsGranted = storagePermission.isGranted)

            FluxTheme(
                isOnline = isOnline,
                customization = customization
            ) {

                FluxNavigation(
                    startingScreen = viewModel.getStartingScreen(),
                    customization = customization,
                    platformEntries = { nav ->
                        entry<Route.Catalog> {
                            CatalogScreen(
                                navigate = { route -> nav.navigate(route) },
                            )
                        }
                        entry<Route.Artwork> { entry ->
                            ArtworkScreen(
                                navigate = { route -> nav.navigate(route) },
                                onBack = { nav.onBack() },
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
                                navigate = { route -> nav.navigate(route) },
                                onBack = { nav.onBack() },
                            )
                        }
                        entry<Route.Search> { entry ->
                            SearchScreen(
                                navigate = { route -> nav.navigate(route) },
                                onBack = { nav.onBack() },
                                withType = entry.withType,
                                withGenre = entry.withGenre
                            )
                        }
                        entry<Route.Player> { entry ->
                            PlayerScreen(
                                params = entry.params,
                                onBack = { nav.onBack() },
                            )
                        }
                        entry<Route.Sources> { entry ->
                            SourcesScreen(
                                navigate = { route ->
                                    nav.clearAndNavigate(route)
                                },
                                fromSetup = entry.fromSetup,
                                onBack = { nav.onBack() },
                            )
                        }
                    },
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