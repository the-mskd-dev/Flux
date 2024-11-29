package com.kaem.flux.typeAdapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.kaem.flux.model.flux.Content
import java.lang.reflect.Type

class ArtworkContentTypeAdapter : JsonSerializer<Content>, JsonDeserializer<Content> {

    override fun serialize(src: Content?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", src?.javaClass?.simpleName)
        jsonObject.add("data", context?.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Content {
        val jsonObject = json?.asJsonObject
        val type = when (val typeName = jsonObject?.get("type")?.asString) {
            "MOVIE" -> Content.MOVIE::class.java
            "SHOW" -> Content.SHOW::class.java
            else -> throw IllegalArgumentException("Unknown type: $typeName")
        }
        return context?.deserialize(jsonObject.get("data"), type)
            ?: throw JsonParseException("Unable to deserialize ArtworkContent")
    }

}