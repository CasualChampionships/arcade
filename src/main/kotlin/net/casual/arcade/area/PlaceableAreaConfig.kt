package net.casual.arcade.area

import com.google.gson.JsonObject
import net.minecraft.server.level.ServerLevel

public interface PlaceableAreaConfig {
    public val id: String

    public fun create(level: ServerLevel): PlaceableArea

    public fun write(): JsonObject
}