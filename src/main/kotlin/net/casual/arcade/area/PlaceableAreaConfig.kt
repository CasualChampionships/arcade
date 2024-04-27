package net.casual.arcade.area

import net.minecraft.server.level.ServerLevel

public interface PlaceableAreaConfig {
    public fun create(level: ServerLevel): PlaceableArea
}