package com.mskd.flux.platform

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource

interface StringProvider {
    suspend fun string(resource: StringResource) : String
    suspend fun string(resource: StringResource, vararg formatArgs: Any) : String
    suspend fun plural(resource: PluralStringResource, quantity: Int) : String
    suspend fun plural(resource: PluralStringResource, quantity: Int, vararg formatArgs: Any) : String
}

class FakeStringProvider : StringProvider {
    override suspend fun string(resource: StringResource): String {
        return "string with resource"
    }

    override suspend fun string(
        resource: StringResource,
        vararg formatArgs: Any
    ): String {
        return "string with resource and args"
    }

    override suspend fun plural(
        resource: PluralStringResource,
        quantity: Int
    ): String {
        return "plural with resource"
    }

    override suspend fun plural(
        resource: PluralStringResource,
        quantity: Int,
        vararg formatArgs: Any
    ): String {
        return "plural with resource and args"
    }

}