package com.kaem.flux.screens.howTo

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
import com.kaem.flux.ui.component.LightText
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.theme.Ui

@Composable
fun HowToScreen(onBackButtonTap: () -> Unit) {

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
        horizontalAlignment = Alignment.Start
    ) {

        FluxTopBar(
            text = "Comment nommer mes fichiers ?",
            onBackButtonTap = onBackButtonTap
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Ui.Space.MEDIUM),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
            horizontalAlignment = Alignment.Start
        ) {

            MediumText(
                text = "Pas de panique, c’est très simple ! Pour que vos vidéos soient bien organisées, voici comment nommer vos fichiers :"
            )

            BoldText(text = "Pour les films :")
            MediumText(text = "Donnez à vos films un nom clair, suivi de l’année si vous le souhaitez. Par exemple :")
            LightText(
                text = "Pulp Fiction (1994).mkv\n" +
                        "\n" +
                        "Inception.avi\n" +
                        "\n" +
                        "Captain-America-The-Winter-Soldier-(2014).mp4"
            )


            BoldText(text = "Pour les séries :")
            MediumText(text = "Pour les épisodes de séries, utilisez un format qui indique la saison et l’épisode. Par exemple :")
            LightText(
                text = "show-name_s01.e02.mkv\n" +
                        "\n" +
                        "show-name_1x02.mkv\n" +
                        "\n" +
                        "show-name_se1.ep2.mkv\n" +
                        "\n" +
                        "show-name-season1.episode2.mkv"
            )


        }

    }

}