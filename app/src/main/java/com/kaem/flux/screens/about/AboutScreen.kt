package com.kaem.flux.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaem.flux.R
import com.kaem.flux.ui.component.BoldText
import com.kaem.flux.ui.component.FluxTopBar
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.theme.Ui

@Composable
fun AboutScreen(onBackButtonTap: () -> Unit) {

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        FluxTopBar(
            text = stringResource(R.string.about),
            onBackButtonTap = onBackButtonTap
        )

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

    }

}

@Composable
fun AboutSection(
    title: String,
    content: String
) {
    Column(
    ) {
        BoldText(text = title)
        MediumText(text = content)
    }
}