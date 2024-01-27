package net.casual.arcade.area

import com.google.gson.JsonObject

public interface PlaceableAreaConfigFactory {
    public val id: String

    public fun create(data: JsonObject): PlaceableAreaConfig
}