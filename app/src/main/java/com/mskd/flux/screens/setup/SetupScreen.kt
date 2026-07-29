package com.mskd.flux.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.mskd.flux.screens.setup.composables.SetupWelcomeContent
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_flux
import org.jetbrains.compose.resources.painterResource

@Composable
fun SetupScreen() {

}

@Composable
fun SetupScreenContent() {

    FluxScaffold(
        title = null,
        onBackTap = null,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {}
            ) {
                Text.Label.Large("Next")
            }
        }
    ) { _ ->

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                modifier = Modifier
                    .sizeIn(maxWidth = 600.dp, maxHeight = 600.dp)
                    .fillMaxSize()
                    .alpha(.15f),
                painter = painterResource(Res.drawable.ic_flux),
                contentDescription = "logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )

            Box(
                modifier = Modifier
                    .padding(FluxUI.Space.medium)
                    .widthIn(max = 600.dp),
            ) {

                SetupWelcomeContent()

            }

        }


    }

}