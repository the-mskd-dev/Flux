package com.mskd.flux.screens.catalog.composable.promo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.modal.dialog.FluxDialog
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.close
import flux.shared.generated.resources.play_store
import flux.shared.generated.resources.play_store_promo_outro
import flux.shared.generated.resources.play_store_promo_support
import flux.shared.generated.resources.play_store_promo_thanks
import flux.shared.generated.resources.play_store_promo_thanks_intro
import org.jetbrains.compose.resources.stringResource

/**
 * One-time announcement of the Play Store release (FOSS flavor only).
 *
 * TODO: Remove this dialog (and its flag, view model logic and strings) by March 2027.
 */
@Composable
fun CatalogPlayStorePromoDialog(
    isVisible: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    FluxDialog(
        isVisible = isVisible,
        onDismiss = { sendIntent(CatalogIntent.OnPlayStorePromoDismiss) },
        onDismissLabel = stringResource(Res.string.close),
        content = {

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
            ) {

                Text.Content.Body(text = stringResource(Res.string.play_store_promo_thanks_intro))
                Text.Content.Body(text = stringResource(Res.string.play_store_promo_thanks))
                Text.Content.Body(text = stringResource(Res.string.play_store_promo_support))

                Button(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = { sendIntent(CatalogIntent.OnPlayStorePromoClick) }
                ) {
                    Text.Button.Default(text = stringResource(Res.string.play_store))
                }

                Text.Content.Body(text = stringResource(Res.string.play_store_promo_outro))

            }

        }
    )

}
