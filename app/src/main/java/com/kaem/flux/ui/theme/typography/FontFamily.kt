package com.kaem.flux.ui.theme.typography

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui

// Region Normal

@OptIn(ExperimentalTextApi::class)
val RobotoFlex = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight),
            FontVariation.grade(100),
            FontVariation.width(40f),
            FontVariation.slant(-2f),
            FontVariation.Setting("XOPQ", 110f), // Thick stroke
            FontVariation.Setting("YOPQ", 20f), // Thin stroke
        ),
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview(widthDp = 500)
@Composable
fun FontFamily_Preview() {
    FluxTheme {
        Column(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Display",
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Text(
                        text = "Display",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    Text(
                        text = "Display",
                        style = MaterialTheme.typography.displaySmall,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Display",
                        style = MaterialTheme.typography.displayLargeEmphasized,
                    )
                    Text(
                        text = "Display",
                        style = MaterialTheme.typography.displayMediumEmphasized,
                    )
                    Text(
                        text = "Display",
                        style = MaterialTheme.typography.displaySmallEmphasized,
                    )
                }

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Headline",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = "Headline",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Headline",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Headline",
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                    )
                    Text(
                        text = "Headline",
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                    )
                    Text(
                        text = "Headline",
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                    )
                }

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleMediumEmphasized,
                    )
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.titleSmallEmphasized,
                    )
                }

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.bodyLargeEmphasized,
                    )
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                    )
                    Text(
                        text = "Body",
                        style = MaterialTheme.typography.bodySmallEmphasized,
                    )
                }

            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Label",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = "Label",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "Label",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Label",
                        style = MaterialTheme.typography.labelLargeEmphasized,
                    )
                    Text(
                        text = "Label",
                        style = MaterialTheme.typography.labelMediumEmphasized,
                    )
                    Text(
                        text = "Label",
                        style = MaterialTheme.typography.labelSmallEmphasized,
                    )
                }

            }

        }
    }
}