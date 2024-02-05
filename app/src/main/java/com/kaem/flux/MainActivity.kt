package com.kaem.flux

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaem.flux.details.DetailsScreen
import com.kaem.flux.home.LibraryScreen
import com.kaem.flux.ui.theme.FluxTheme
import dagger.hilt.android.AndroidEntryPoint

object Screen {
    const val LIBRARY = "library"
    const val DETAILS = "details"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {

            FluxTheme {

                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = Screen.LIBRARY) {

                    composable(Screen.LIBRARY) {
                        LibraryScreen(
                            navigateToDetails = { navController.navigate(Screen.DETAILS) }
                        )
                    }

                    composable(Screen.DETAILS) { DetailsScreen() }

                }

            }

        }

    }

}