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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@Composable
fun HowToScreen(onBack: () -> Unit) {

    FluxScaffold(
        title = stringResource(R.string.how_to_name_files),
        onBackTap = onBack
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Ui.Space.MEDIUM),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
                horizontalAlignment = Alignment.Start
            ) {

                Text.Body.Large(text = stringResource(R.string.how_to_name_files_desc))

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
        verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
        horizontalAlignment = Alignment.Start
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)) {

            Text.Title.Large(text = stringResource(R.string.movies), emphasized = true)
            Text.Body.Large(text = stringResource(R.string.how_to_name_files_movies_desc))

            Column(
                modifier = Modifier.alpha(.7f),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
            ) {
                Text.Body.Medium(text = "• " + stringResource(R.string.movie_file_example_1))
                Text.Body.Medium(text = "• " + stringResource(R.string.movie_file_example_2))
                Text.Body.Medium(text = "• " + stringResource(R.string.movie_file_example_3))

            }

        }

        Column(verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)) {

            Text.Title.Large(text = stringResource(R.string.shows), emphasized = true)
            Text.Body.Large(text = stringResource(R.string.how_to_name_files_show_desc))

            Column(
                modifier = Modifier.alpha(.7f),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
            ) {
                Text.Body.Medium(text = "• " + stringResource(R.string.show_file_example_1))
                Text.Body.Medium(text = "• " + stringResource(R.string.show_file_example_2))
                Text.Body.Medium(text = "• " + stringResource(R.string.show_file_example_3))
                Text.Body.Medium(text = "• " + stringResource(R.string.show_file_example_4))
                Text.Body.Medium(text = "• " + stringResource(R.string.show_file_example_5))
            }

        }

    }

}

@FluxPreview
@Composable
fun HowToScreen_Preview() {
    AppTheme {
        HowToScreen(onBack = {})
    }
}