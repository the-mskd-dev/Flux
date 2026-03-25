package com.kaem.flux.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.FluxPreview

@Composable
fun AboutScreen(onBack: () -> Unit) {

    FluxScaffold(
        title = stringResource(R.string.about),
        onBackTap = onBack
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = Ui.Space.LARGE),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Ui.Space.MEDIUM),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
                horizontalAlignment = Alignment.Start
            ) {

                AboutSection(
                    title = stringResource(R.string.who_am_i),
                    content = stringResource(R.string.who_am_i_desc),
                )

                AboutSection(
                    title = stringResource(R.string.why_this_app),
                    content = stringResource(R.string.why_this_app_desc),
                )

                AboutSection(
                    title = stringResource(R.string.how_it_works),
                    content = stringResource(R.string.how_it_works_desc)
                )

                AboutSection(
                    title = stringResource(R.string.which_technologies),
                    content = stringResource(R.string.which_technologies_desc)
                )

                AboutSection(
                    title = stringResource(R.string.what_is_next),
                    content = stringResource(R.string.what_is_next_desc)
                )

                AboutSection(
                    title = stringResource(R.string.how_to_suggest),
                    content = stringResource(R.string.how_to_suggest_desc)
                )

            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}

@Composable
fun AboutSection(
    title: String,
    content: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)) {
        Text.Headline.Small(text = title)
        Text.Body.Large(text = content)
    }
}

@FluxPreview
@Composable
fun AboutScreen_Preview() {
    AppTheme {
        AboutScreen {  }
    }
}