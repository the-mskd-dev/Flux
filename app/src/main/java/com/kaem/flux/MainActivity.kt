package com.kaem.flux

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaem.flux.details.DetailsScreen
import com.kaem.flux.home.LibraryScreen
import com.kaem.flux.ui.theme.FluxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {

            FluxTheme {

                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "library") {

                    composable("library") {
                        LibraryScreen(
                            navigateToDetails = {
                                navController.navigate("details/$it")
                            }
                        )
                    }

                    composable(
                        "details/{artworkId}",
                        arguments = listOf(navArgument("artworkId") { type = NavType.IntType })
                    ) {
                        DetailsScreen(
                            artworkId = it.arguments?.getInt("artworkId", -1) ?: -1
                        )
                    }

                }

            }

        }

    }

}