package com.kaem.flux.typeAdapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.kaem.flux.model.flux.ArtworkContent
import java.lang.reflect.Type

class ArtworkContentTypeAdapter : JsonSerializer<ArtworkContent>, JsonDeserializer<ArtworkContent> {

    override fun serialize(src: ArtworkContent?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", src?.javaClass?.simpleName)
        jsonObject.add("data", context?.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ArtworkContent {
        val jsonObject = json?.asJsonObject
        val type = when (val typeName = jsonObject?.get("type")?.asString) {
            "MOVIE" -> ArtworkContent.MOVIE::class.java
            "SHOW" -> ArtworkContent.SHOW::class.java
            else -> throw IllegalArgumentException("Unknown type: $typeName")
        }
        return context?.deserialize(jsonObject.get("data"), type)
            ?: throw JsonParseException("Unable to deserialize ArtworkContent")
    }

}