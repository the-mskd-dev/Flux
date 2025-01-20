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
import androidx.compose.ui.tooling.preview.Preview
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            ) {

                BoldText(text = "Pour les films :")
                MediumText(text = "Donnez à vos films un nom clair, suivi de l’année si vous le souhaitez. Par exemple :")
                LightText(
                    text = "Spider-man (2002).mkv\n" +
                            "Your name.avi\n" +
                            "Spider-man-no-way-home-(2021).mp4"
                )

            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            ) {

                BoldText(text = "Pour les séries :")
                MediumText(text = "Pour les épisodes de séries, utilisez un format qui indique la saison et l’épisode. Par exemple :")
                LightText(
                    text = "nom série_s01.e02.mkv\n" +
                            "nom_série_1x02.mkv\n" +
                            "nom_série_se1.ep2.mkv\n" +
                            "nom_série-season1.episode2.mkv"
                )

            }


        }

    }

}

@Preview
@Composable
fun HowToScreen_Preview() {
    HowToScreen(onBackButtonTap = {})
}