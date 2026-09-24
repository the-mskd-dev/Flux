package com.mskd.flux.screens.message

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.message_details
import flux.shared.generated.resources.message_title
import flux.shared.generated.resources.participate
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MessageScreen(onBack: () -> Unit) {

    FluxScaffold(
        title = stringResource(Res.string.message_title),
        onBackTap = onBack
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = FluxUI.Space.medium),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                horizontalAlignment = Alignment.Start
            ) {

                stringArrayResource(Res.array.message_details).forEach {
                    Text.Content.Body(text = it)
                }

            }

            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { },
            ) {
                Text.Button.Default(stringResource(Res.string.participate))
            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}

@FluxPreview
@Composable
fun MessageScreen_Preview() {
    FluxThemePreview {
        MessageScreen {}
    }
}