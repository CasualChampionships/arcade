/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.extensions

import net.casual.arcade.border.ducks.SerializableBorder
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

internal class BorderSerializerExtension(
    private val level: ServerLevel
): DataExtension {
    override fun getName(): String {
        return "${ArcadeUtils.MOD_ID}_border_serializer"
    }

    override fun serialize(): Tag {
        return (this.level.worldBorder as SerializableBorder).`arcade$serialize`()
    }

    override fun deserialize(element: Tag) {
        element as CompoundTag
        GlobalTickedScheduler.later {
            (this.level.worldBorder as SerializableBorder).`arcade$deserialize`(element)
        }
    }

    companion object {
        fun registerEvents() {
            GlobalEventHandler.Server.register<LevelExtensionEvent> { event ->
                event.addExtension(::BorderSerializerExtension)
            }
        }
    }
}