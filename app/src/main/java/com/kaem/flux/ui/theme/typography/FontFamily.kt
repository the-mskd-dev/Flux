package com.kaem.flux.ui.theme.typography

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.kaem.flux.screens.home.HomeIntent
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui

// Region Normal

@OptIn(ExperimentalTextApi::class)
val RobotoFlexDisplay = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexHeadline = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexTitle = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexBody = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexLabel = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

//endregion

//region Emphasized

@OptIn(ExperimentalTextApi::class)
val RobotoFlexDisplayEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexHeadlineEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexTitleEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexBodyEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

@OptIn(ExperimentalTextApi::class)
val RobotoFlexLabelEmphasized = FontFamily(
    Font(
        resId = R.font.roboto_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
            //TODO
        ),
    )
)

//endregion


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
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