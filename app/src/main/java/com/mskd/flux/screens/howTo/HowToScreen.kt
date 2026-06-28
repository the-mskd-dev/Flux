package com.mskd.flux.screens.howTo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.how_to_advice
import flux.shared.generated.resources.how_to_name_files
import flux.shared.generated.resources.how_to_name_files_desc
import flux.shared.generated.resources.how_to_name_files_movies_desc
import flux.shared.generated.resources.how_to_name_files_show_desc
import flux.shared.generated.resources.movie_file_example_1
import flux.shared.generated.resources.movie_file_example_2
import flux.shared.generated.resources.movie_file_example_3
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.show_file_example_1
import flux.shared.generated.resources.show_file_example_2
import flux.shared.generated.resources.show_file_example_3
import flux.shared.generated.resources.show_file_example_4
import flux.shared.generated.resources.show_file_example_5
import flux.shared.generated.resources.show_file_example_6
import flux.shared.generated.resources.shows
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToScreen(onBack: () -> Unit) {

    FluxScaffold(
        title = stringResource(Res.string.how_to_name_files),
        onBackTap = onBack
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FluxUI.Space.medium),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large),
                horizontalAlignment = Alignment.Start
            ) {

                Text.Body.Large(text = stringResource(Res.string.how_to_name_files_desc))

                HowToNameFiles()

            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}

@Composable
fun HowToNameFiles() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large),
        horizontalAlignment = Alignment.Start
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {

            Text.Title.Large(text = stringResource(Res.string.movies), emphasized = true)
            Text.Body.Large(text = stringResource(Res.string.how_to_name_files_movies_desc))

            Column(
                modifier = Modifier.alpha(.7f),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
            ) {
                Text.Body.Medium(text = "• " + stringResource(Res.string.movie_file_example_1))
                Text.Body.Medium(text = "• " + stringResource(Res.string.movie_file_example_2))
                Text.Body.Medium(text = "• " + stringResource(Res.string.movie_file_example_3))

            }

        }

        Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {

            Text.Title.Large(text = stringResource(Res.string.shows), emphasized = true)
            Text.Body.Large(text = stringResource(Res.string.how_to_name_files_show_desc))

            Column(
                modifier = Modifier.alpha(.7f),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
            ) {
                Text.Body.Medium(text = "• " + stringResource(Res.string.show_file_example_1))
                Text.Body.Medium(text = "• " + stringResource(Res.string.show_file_example_2))
                Text.Body.Medium(text = "• " + stringResource(Res.string.show_file_example_3))
                Text.Body.Medium(text = "• " + stringResource(Res.string.show_file_example_4))
                Text.Body.Medium(text = "• " + stringResource(Res.string.show_file_example_5))
                Text.Body.Medium(text = "• " + stringResource(Res.string.show_file_example_6))
            }

        }

        Text.Body.Large(stringResource(Res.string.how_to_advice))



    }

}

@FluxPreview
@Composable
fun HowToScreen_Preview() {
    FluxTheme {
        HowToScreen(onBack = {})
    }
}