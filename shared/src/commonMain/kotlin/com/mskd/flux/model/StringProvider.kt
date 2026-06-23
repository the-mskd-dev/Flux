package com.mskd.flux.model

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

sealed class StringProvider {
    data class Static(val label: String): StringProvider()
    data class Resource(val resource: StringResource): StringProvider()
    data class ResourceWithArgs(val resource: StringResource, val args: List<Any>): StringProvider()
    data class Plural(val resource: PluralStringResource, val quantity: Int): StringProvider()
    data class PluraleWithArgs(val resource: PluralStringResource, val quantity: Int, val args: List<Any>): StringProvider()
}