package com.mskd.flux.platform

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

class AndroidStringProvider: StringProvider {
    override suspend fun string(resource: StringResource): String {
        return getString(resource)
    }

    override suspend fun string(
        resource: StringResource,
        vararg formatArgs: Any
    ): String {
        return getString(resource, formatArgs)
    }

    override suspend fun plural(
        resource: PluralStringResource,
        quantity: Int
    ): String {
        return getPluralString(resource, quantity)
    }

    override suspend fun plural(
        resource: PluralStringResource,
        quantity: Int,
        vararg formatArgs: Any
    ): String {
        return getPluralString(resource, quantity, formatArgs)
    }
}