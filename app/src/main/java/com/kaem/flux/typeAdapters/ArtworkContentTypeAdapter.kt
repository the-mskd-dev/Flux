package com.kaem.flux.typeAdapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.kaem.flux.model.flux.ArtworkType
import java.lang.reflect.Type

class ArtworkContentTypeAdapter : JsonSerializer<ArtworkType>, JsonDeserializer<ArtworkType> {

    override fun serialize(src: ArtworkType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", src?.javaClass?.simpleName)
        jsonObject.add("data", context?.serialize(src))
        return jsonObject
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ArtworkType {
        val jsonObject = json?.asJsonObject
        val type = when (val typeName = jsonObject?.get("type")?.asString) {
            "MOVIE" -> ArtworkType.MOVIE::class.java
            "SHOW" -> ArtworkType.SHOW::class.java
            else -> throw IllegalArgumentException("Unknown type: $typeName")
        }
        return context?.deserialize(jsonObject.get("data"), type)
            ?: throw JsonParseException("Unable to deserialize ArtworkContent")
    }

}