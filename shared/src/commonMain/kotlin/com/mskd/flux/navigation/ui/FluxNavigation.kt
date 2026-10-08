package com.mskd.flux.navigation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mskd.flux.features.about.ui.AboutScreen
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.features.customization.ui.CustomizationScreen
import com.mskd.flux.features.howTo.ui.HowToScreen
import com.mskd.flux.features.message.ui.MessageScreen
import com.mskd.flux.features.privateFolder.ui.PrivateScreen
import com.mskd.flux.features.settings.ui.SettingsScreen
import com.mskd.flux.features.setup.ui.SetupScreen
import com.mskd.flux.features.show.ui.ShowScreen
import com.mskd.flux.features.token.ui.TokenScreen
import com.mskd.flux.navigation.domain.FluxNavigator
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.popScreen
import com.mskd.flux.report.CrashLogger
import com.mskd.flux.ui.theme.createColorScheme
import org.koin.compose.koinInject

@Composable
fun FluxNavigation(
    backStack: NavBackStack<NavKey>,
    platformEntries: EntryProviderScope<NavKey>.(FluxNavigator) -> Unit = {},
    customization: CustomizationDataStore.State
) {

    val crashLogger = koinInject<CrashLogger>()
    var transitions by remember { mutableStateOf(Transition.Forward to Transition.Backward) }

    val navigator = remember(backStack) {
        FluxNavigator(
            navigate = { route ->
                transitions = Transition.Forward to Transition.Backward
                crashLogger.addBreadcrumb(message = "navigate to $route")
                backStack.add(route)
            },
            onBack = {
                transitions = Transition.Forward to Transition.Backward
                crashLogger.addBreadcrumb(message = "pop to ${backStack.getOrNull(backStack.lastIndex - 1) as? Route}")
                backStack.popScreen()
            },
            clearAndNavigate = { route ->
                backStack.clear()
                crashLogger.addBreadcrumb(message = "clear backstack then navigate to $route")
                backStack.add(route)
            },
        )
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = { transitions.first },
        popTransitionSpec = { transitions.second },
        predictivePopTransitionSpec = { transitions.second },
        entryProvider = entryProvider {
            entry<Route.Setup> {
                SetupScreen(
                    navigate = { route ->
                        backStack.clear()
                        navigator.navigate(route)
                    },
                )
            }
            entry<Route.Show> { entry ->
                ShowScreen(
                    navigate = { route -> navigator.navigate(route) },
                    onBack = { navigator.onBack() },
                    artworkId = entry.artworkId,
                    colorScheme = createColorScheme(
                        theme = customization.uiTheme,
                        color = customization.color ?: entry.rgb
                    )
                )
            }
            entry<Route.PrivateFolder> {
                PrivateScreen(
                    navigate = { route -> navigator.navigate(route) },
                    onBack = { navigator.onBack() },
                )
            }
            entry<Route.Settings> {
                SettingsScreen(
                    navigate = { route -> navigator.navigate(route) },
                    onBack = { navigator.onBack() },
                )
            }
            entry<Route.Customization> {
                CustomizationScreen(
                    onBack = { navigator.onBack() },
                )
            }
            entry<Route.HowTo> {
                HowToScreen(
                    onBack = { navigator.onBack() }
                )
            }
            entry<Route.About> {
                AboutScreen(
                    onBack = { navigator.onBack() }
                )
            }
            entry<Route.Token> { entry ->
                TokenScreen(
                    onBack = { navigator.onBack() },
                    navigate = { route ->
                        backStack.clear()
                        navigator.navigate(route)
                    },
                    fromSetup = entry.fromSetup
                )
            }
            entry<Route.Message> {
                MessageScreen(
                    onBack = { navigator.onBack() }
                )
            }

            platformEntries(navigator)

        },
    )


}