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
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaem.flux.screens.details.DetailsScreen
import com.kaem.flux.screens.home.LibraryScreen
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
                
                NavHost(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background),
                    navController = navController,
                    startDestination = "library"
                ) {

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