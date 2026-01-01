/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util

import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.item.MapItem
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.maps.MapId
import net.minecraft.world.level.saveddata.maps.MapItemSavedData

internal object ReplayPacketUtils {
    fun createMapPacket(id: MapId, level: Level): ClientboundMapItemDataPacket? {
        val data = MapItem.getSavedData(id, level) ?: return null
        val patch = MapItemSavedData.MapPatch(0, 0, 128, 128, data.colors.copyOf())
        return ClientboundMapItemDataPacket(id, data.scale, data.locked, data.decorations.toList(), patch)
    }
}