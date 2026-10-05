package com.mskd.flux.ui.text

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

sealed class TextProvider {
    data class Static(val label: String): TextProvider()
    data class Resource(val resource: StringResource): TextProvider()
    data class ResourceWithArgs(val resource: StringResource, val args: List<Any>): TextProvider()
    data class Plural(val resource: PluralStringResource, val quantity: Int): TextProvider()
    data class PluralWithArgs(val resource: PluralStringResource, val quantity: Int, val args: List<Any>): TextProvider();

    @Composable
    fun resolve() : String = when (this) {
        is Plural -> pluralStringResource(resource = this.resource, quantity = this.quantity)
        is PluralWithArgs -> pluralStringResource(resource = this.resource, quantity = this.quantity, formatArgs = this.args.toTypedArray())
        is Resource -> stringResource(resource = this.resource)
        is ResourceWithArgs -> stringResource(resource = this.resource, formatArgs = this.args.toTypedArray())
        is Static -> this.label
    }
}