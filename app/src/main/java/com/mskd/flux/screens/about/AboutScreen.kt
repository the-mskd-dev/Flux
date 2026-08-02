package com.mskd.flux.screens.about

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.buildLinkedString
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.about
import flux.shared.generated.resources.github
import flux.shared.generated.resources.how_it_works
import flux.shared.generated.resources.how_it_works_desc
import flux.shared.generated.resources.how_to_suggest
import flux.shared.generated.resources.how_to_suggest_desc
import flux.shared.generated.resources.mail
import flux.shared.generated.resources.what_is_next
import flux.shared.generated.resources.what_is_next_desc
import flux.shared.generated.resources.which_technologies
import flux.shared.generated.resources.which_technologies_desc
import flux.shared.generated.resources.who_am_i
import flux.shared.generated.resources.who_am_i_desc
import flux.shared.generated.resources.why_this_app
import flux.shared.generated.resources.why_this_app_desc
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {

    FluxScaffold(
        title = stringResource(Res.string.about),
        onBackTap = onBack
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = FluxUI.Space.large),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FluxUI.Space.medium),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large),
                horizontalAlignment = Alignment.Start
            ) {

                AboutSection(
                    title = stringResource(Res.string.why_this_app),
                    content = stringResource(Res.string.why_this_app_desc),
                )

                AboutSection(
                    title = stringResource(Res.string.who_am_i),
                    content = stringResource(Res.string.who_am_i_desc),
                )

                AboutSection(
                    title = stringResource(Res.string.how_it_works),
                    content = stringResource(Res.string.how_it_works_desc)
                )

                AboutSection(
                    title = stringResource(Res.string.which_technologies),
                    content = stringResource(Res.string.which_technologies_desc)
                )

                AboutSection(
                    title = stringResource(Res.string.what_is_next),
                    content = stringResource(Res.string.what_is_next_desc)
                )

                AboutSectionWithLinks(
                    title = stringResource(Res.string.how_to_suggest),
                    content = buildLinkedString(
                        template = stringResource(Res.string.how_to_suggest_desc),
                        stringResource(Res.string.mail) to "mailto:" + Constants.CONTACT.MAIL,
                        stringResource(Res.string.github) to Constants.CONTACT.ISSUES
                    )
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
    Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {
        Text.Content.Title(text = title)
        Text.Content.Body(text = content)
    }
}

@Composable
fun AboutSectionWithLinks(
    title: String,
    content: AnnotatedString
) {
    Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)) {
        Text.Content.Title(text = title)
        Text.Annotated(
            text = content,
            style = Text.Style.contentBody()
        )
    }
}

@FluxPreview
@Composable
fun AboutScreen_Preview() {
    FluxTheme {
        AboutScreen {  }
    }
}