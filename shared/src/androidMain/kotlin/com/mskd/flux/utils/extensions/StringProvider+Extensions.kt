package com.mskd.flux.utils.extensions

import androidx.compose.runtime.Composable
import com.mskd.flux.core.model.core.StringProvider
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StringProvider.resolve() : String = when (this) {
    is StringProvider.Plural -> pluralStringResource(resource = this.resource, quantity = this.quantity)
    is StringProvider.PluraleWithArgs -> pluralStringResource(resource = this.resource, quantity = this.quantity, formatArgs = this.args.toTypedArray())
    is StringProvider.Resource -> stringResource(resource = this.resource)
    is StringProvider.ResourceWithArgs -> stringResource(resource = this.resource, formatArgs = this.args.toTypedArray())
    is StringProvider.Static -> this.label
}