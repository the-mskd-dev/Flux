package com.kaem.flux

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.navigation.Route
import com.kaem.flux.navigation.Transition
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
import com.kaem.flux.utils.Constants
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

    private fun observeDataStore() {
        lifecycleScope.launch {
            dataStoreRepository.flow.collect {
                uiTheme = it.uiTheme
            }
        }
    }

}